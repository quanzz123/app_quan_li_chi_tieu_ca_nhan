package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import androidx.annotation.NonNull;

import android.util.Log;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.ImageView;
import java.util.UUID;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import com.example.app_quan_li_chi_tieu_ca_nhan.api.ImgBBResponse;
import com.example.app_quan_li_chi_tieu_ca_nhan.api.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity cho phép thêm mới giao dịch chi tiêu một cách thủ công.
 * Hỗ trợ chọn danh mục (Drinks, Food, Shopping), chọn ảnh minh họa giao dịch từ máy,
 * nén ảnh để tránh tràn RAM, tải ảnh lên ImgBB và lưu giao dịch vào Firestore (đồng bộ giảm số dư ví).
 */
public class AddActivity extends AppCompatActivity {

    private EditText edtTitle, edtAmount, edtDate;
    private ChipGroup chipCategories;
    private ImageView ivTransactionImage;
    private ProgressBar imageUploadProgressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private String uploadedImageUrl = null;
    
    // Khóa API mặc định để upload ảnh lên ImgBB
    private static final String IMGBB_API_KEY = "ae3d333d8018f466778a1302f71eb896";

    // Launcher mở trình chọn hình ảnh từ bộ nhớ thiết bị
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivTransactionImage.setImageURI(uri);
                    ivTransactionImage.setPadding(0, 0, 0, 0); // Bỏ padding khi đã hiển thị ảnh
                    ivTransactionImage.setImageTintList(null); // Bỏ màu phủ tint
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        edtTitle = findViewById(R.id.edtTitle);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        
        // Mặc định điền ngày hiện tại dưới dạng dd/MM/yyyy
        String currentDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        edtDate.setText(currentDate);
        chipCategories = findViewById(R.id.chipCategories);
        ivTransactionImage = findViewById(R.id.ivTransactionImage);
        imageUploadProgressBar = findViewById(R.id.imageUploadProgressBar);

        // Thiết lập sự kiện chọn hình ảnh khi click vào CardAddImage
        findViewById(R.id.cardAddImage).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        // Cho phép nhấn giữ để gỡ bỏ ảnh đã chọn
        findViewById(R.id.cardAddImage).setOnLongClickListener(v -> {
            if (selectedImageUri != null) {
                selectedImageUri = null;
                ivTransactionImage.setImageResource(android.R.drawable.ic_menu_camera);
                ivTransactionImage.setPadding(32, 32, 32, 32); // Phục hồi padding cho icon camera
                ivTransactionImage.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#64748B"))); // Phục hồi tint màu
                Toast.makeText(this, "Đã gỡ ảnh", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Nếu được mở kèm tên danh mục truyền từ Intent (ví dụ: nhấn từ Grid Trang chủ)
        String categoryName = getIntent().getStringExtra("categoryName");
        if (categoryName != null) {
            selectChipByCategory(categoryName);
        }

        // Đảm bảo không bị che khuất bởi system navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft() + bars.left,
                    v.getPaddingTop() + bars.top,
                    v.getPaddingRight() + bars.right,
                    v.getPaddingBottom() + bars.bottom
            );
            return insets;
        });

        // Xử lý sự kiện nhấn nút Lưu giao dịch
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(this, "Lỗi: Bạn cần đăng nhập để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu người dùng có chọn ảnh minh họa, upload trước rồi mới lưu giao dịch
            if (selectedImageUri != null) {
                uploadImageAndSaveTransaction(userId);
            } else {
                saveTransaction();
            }
        });
    }

    /**
     * Nén ảnh thông minh theo tỉ lệ và gửi request upload lên ImgBB.
     */
    private void uploadImageAndSaveTransaction(String userId) {
        findViewById(R.id.btnSave).setEnabled(false);
        imageUploadProgressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                // 1. Lấy kích thước ảnh trước khi decode thật để tránh lãng phí RAM
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream isSize = getContentResolver().openInputStream(selectedImageUri);
                BitmapFactory.decodeStream(isSize, null, options);
                isSize.close();

                // 2. Tính toán tỷ lệ nén (Scale) để ảnh không vượt quá khoảng 1500px tránh lỗi tràn bộ nhớ (Out of Memory)
                int reqWidth = 1500;
                int reqHeight = 1500;
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;

                // 3. Tiến hành giải mã (decode) ảnh thật dựa trên tỷ lệ nén vừa tính
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                
                if (bitmap == null) {
                    throw new Exception("Không thể đọc dữ liệu ảnh");
                }

                // 4. Kiểm tra góc xoay EXIF để xoay ảnh về đúng chiều
                bitmap = rotateImageIfRequired(bitmap, selectedImageUri);
                
                Log.d("AddActivity", "Ảnh gốc: " + options.outWidth + "x" + options.outHeight + " -> Đã xử lý: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                // Nén ảnh chất lượng JPEG 70%
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] data = baos.toByteArray();
                bitmap.recycle(); // Thu hồi vùng nhớ của bitmap ngay lập tức

                // Chạy hàm upload trên UI Thread (Retrofit gọi bất đồng bộ)
                runOnUiThread(() -> performImgBBUpload(data));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    imageUploadProgressBar.setVisibility(View.GONE);
                    findViewById(R.id.btnSave).setEnabled(true);
                    Log.e("AddActivity", "Lỗi xử lý ảnh lớn", e);
                    Toast.makeText(AddActivity.this, "Lỗi xử lý ảnh lớn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Kiểm tra góc quay EXIF của tệp ảnh để tránh ảnh bị quay ngược khi hiển thị.
     */
    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws Exception {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (android.os.Build.VERSION.SDK_INT > 23) {
            ei = new ExifInterface(input);
        } else {
            ei = new ExifInterface(selectedImage.getPath());
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /**
     * Tính kích thước nén (inSampleSize) tối ưu cho hình ảnh đầu vào.
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Tiến hành gửi Multipart request upload hình ảnh đã nén lên máy chủ ImgBB.
     */
    private void performImgBBUpload(byte[] data) {
        try {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), data);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "transaction.jpg", requestFile);

            Log.d("AddActivity", "Đang gửi dữ liệu lên ImgBB...");
            RetrofitClient.getImgBBApi().uploadImage(IMGBB_API_KEY, body).enqueue(new Callback<ImgBBResponse>() {
                @Override
                public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                    if (isFinishing()) return;
                    
                    imageUploadProgressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        if (response.body().getData() != null) {
                            uploadedImageUrl = response.body().getData().getUrl();
                            Log.d("AddActivity", "Upload ImgBB thành công: " + uploadedImageUrl);
                            // Lưu giao dịch vào Firestore sau khi upload ảnh thành công
                            saveTransaction();
                        } else {
                            handleUploadError("Lỗi dữ liệu: ImgBB không trả về link ảnh");
                        }
                    } else {
                        String errorMsg = "Lỗi ImgBB: " + response.code();
                        if (response.code() == 401) errorMsg = "Lỗi: API Key ImgBB không hợp lệ hoặc đã hết hạn";
                        handleUploadError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                    if (isFinishing()) return;
                    imageUploadProgressBar.setVisibility(View.GONE);
                    handleUploadError("Lỗi kết nối ImgBB: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            handleUploadError("Lỗi chuẩn bị upload: " + e.getMessage());
        }
    }

    private void handleUploadError(String message) {
        findViewById(R.id.btnSave).setEnabled(true);
        Log.e("AddActivity", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Thực hiện Lưu giao dịch chi tiêu vào cơ sở dữ liệu Firestore trong một Transaction an toàn.
     * Transaction đảm bảo ghi giao dịch mới đồng thời giảm số dư ví thành công, nếu 1 trong 2 lỗi thì rollback.
     */
    private void saveTransaction() {
        String title = edtTitle.getText().toString().trim();
        String amountStr = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        
        int checkedChipId = chipCategories.getCheckedChipId();
        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            findViewById(R.id.btnSave).setEnabled(true);
            return;
        }
        if (amountStr.isEmpty()) {
            edtAmount.setError("Vui lòng nhập số tiền");
            findViewById(R.id.btnSave).setEnabled(true);
            return;
        }
        if (checkedChipId == View.NO_ID) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnSave).setEnabled(true);
            return;
        }

        Chip selectedChip = findViewById(checkedChipId);
        String category = selectedChip.getText().toString();
        double amount = Double.parseDouble(amountStr);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnSave).setEnabled(true);
            return;
        }

        findViewById(R.id.btnSave).setEnabled(false);
        Toast.makeText(this, "Đang lưu giao dịch...", Toast.LENGTH_SHORT).show();

        // Khởi chạy Transaction Firestore
        db.runTransaction(transaction -> {
            DocumentReference balanceRef = db.collection("balances").document(userId);
            DocumentReference newTransactionRef = db.collection("transactions").document();

            Map<String, Object> txData = new HashMap<>();
            txData.put("userId", userId);
            txData.put("title", title);
            txData.put("categoryName", category);
            txData.put("amount", amount);
            txData.put("date", date);
            txData.put("timestamp", System.currentTimeMillis());
            txData.put("isExpense", true); // Mặc định ở đây luôn là khoản chi tiêu
            if (uploadedImageUrl != null) {
                txData.put("imageUrl", uploadedImageUrl);
            }

            // Ghi dữ liệu giao dịch
            transaction.set(newTransactionRef, txData);
            // Cập nhật số dư (Trừ tiền) ví điện tử
            transaction.update(balanceRef, "currentBalance", FieldValue.increment(-amount));
            transaction.update(balanceRef, "lastUpdated", System.currentTimeMillis());

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(AddActivity.this, "Đã lưu giao dịch và cập nhật số dư!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            findViewById(R.id.btnSave).setEnabled(true);
            // Nếu tài liệu số dư 'balance' chưa được khởi tạo cho tài khoản này
            if (e.getMessage() != null && e.getMessage().contains("NOT_FOUND")) {
                initializeBalanceAndRetry(userId, title, category, amount, date);
            } else {
                Toast.makeText(AddActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Khởi tạo tài liệu balances cho người dùng mới và thử lưu lại giao dịch.
     */
    private void initializeBalanceAndRetry(String userId, String title, String category, double amount, String date) {
        Map<String, Object> initialBalance = new HashMap<>();
        initialBalance.put("userId", userId);
        initialBalance.put("currentBalance", 0.0);
        initialBalance.put("lastUpdated", System.currentTimeMillis());
        initialBalance.put("currency", "VND");

        db.collection("balances").document(userId)
                .set(initialBalance)
                .addOnSuccessListener(aVoid -> saveTransaction()) // Thử lại sau khi đã khởi tạo
                .addOnFailureListener(e -> {
                    findViewById(R.id.btnSave).setEnabled(true);
                    Toast.makeText(this, "Không thể khởi tạo số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Tự động tick chọn Chip tương ứng với danh mục truyền vào.
     */
    private void selectChipByCategory(String categoryName) {
        for (int i = 0; i < chipCategories.getChildCount(); i++) {
            View child = chipCategories.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(categoryName)) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }
}
