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
    
    // TODO: Thay thế bằng API Key ImgBB của bạn (Lấy tại https://api.imgbb.com/)
    private static final String IMGBB_API_KEY = "ae3d333d8018f466778a1302f71eb896";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivTransactionImage.setImageURI(uri);
                    ivTransactionImage.setPadding(0, 0, 0, 0); // Remove padding when image is shown
                    ivTransactionImage.setImageTintList(null); // Remove tint
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
        chipCategories = findViewById(R.id.chipCategories);
        ivTransactionImage = findViewById(R.id.ivTransactionImage);
        imageUploadProgressBar = findViewById(R.id.imageUploadProgressBar);

        findViewById(R.id.cardAddImage).setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        findViewById(R.id.cardAddImage).setOnLongClickListener(v -> {
            if (selectedImageUri != null) {
                selectedImageUri = null;
                ivTransactionImage.setImageResource(android.R.drawable.ic_menu_camera);
                ivTransactionImage.setPadding(32, 32, 32, 32); // Restore padding
                ivTransactionImage.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#64748B"))); // Restore tint
                Toast.makeText(this, "Đã gỡ ảnh", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        String categoryName = getIntent().getStringExtra("categoryName");
        if (categoryName != null) {
            selectChipByCategory(categoryName);
        }

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

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(this, "Lỗi: Bạn cần đăng nhập để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageAndSaveTransaction(userId);
            } else {
                saveTransaction();
            }
        });
    }

    private void uploadImageAndSaveTransaction(String userId) {
        findViewById(R.id.btnSave).setEnabled(false);
        imageUploadProgressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                // 1. Lấy kích thước ảnh trước khi decode để tránh tốn RAM
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream isSize = getContentResolver().openInputStream(selectedImageUri);
                BitmapFactory.decodeStream(isSize, null, options);
                isSize.close();

                // 2. Tính toán tỷ lệ nén (Scale) để không vượt quá giới hạn bộ nhớ
                // Mục tiêu: Giảm kích thước xuống khoảng 1500-2000px là đủ cho hóa đơn
                int reqWidth = 1500;
                int reqHeight = 1500;
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;

                // 3. Decode ảnh thật với tỷ lệ đã tính
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
                
                if (bitmap == null) {
                    throw new Exception("Không thể đọc dữ liệu ảnh");
                }

                // 4. Xử lý xoay ảnh dựa trên EXIF
                bitmap = rotateImageIfRequired(bitmap, selectedImageUri);
                
                Log.d("AddActivity", "Ảnh gốc: " + options.outWidth + "x" + options.outHeight + " -> Đã xử lý: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] data = baos.toByteArray();
                bitmap.recycle();

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

    // Hàm xoay ảnh dựa trên thông tin EXIF
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

    // Hàm tính toán tỷ lệ giảm kích thước ảnh
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

    private void performImgBBUpload(byte[] data) {
        try {
            // Tạo RequestBody cho Retrofit
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
            txData.put("isExpense", true);
            if (uploadedImageUrl != null) {
                txData.put("imageUrl", uploadedImageUrl);
            }

            transaction.set(newTransactionRef, txData);
            transaction.update(balanceRef, "currentBalance", FieldValue.increment(-amount));
            transaction.update(balanceRef, "lastUpdated", System.currentTimeMillis());

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(AddActivity.this, "Đã lưu giao dịch và cập nhật số dư!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            findViewById(R.id.btnSave).setEnabled(true);
            if (e.getMessage() != null && e.getMessage().contains("NOT_FOUND")) {
                initializeBalanceAndRetry(userId, title, category, amount, date);
            } else {
                Toast.makeText(AddActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

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
