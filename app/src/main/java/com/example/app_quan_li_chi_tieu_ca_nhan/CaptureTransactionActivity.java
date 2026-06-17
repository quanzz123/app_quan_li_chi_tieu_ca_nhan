package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.example.app_quan_li_chi_tieu_ca_nhan.api.ImgBBResponse;
import com.example.app_quan_li_chi_tieu_ca_nhan.api.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity hỗ trợ chụp ảnh hóa đơn/sản phẩm (Camera).
 * Quy trình xử lý:
 * 1. Chụp ảnh lưu tạm vào bộ nhớ thiết bị.
 * 2. Upload ảnh lên hosting ImgBB lấy URL trực tuyến.
 * 3. Chạy phân loại ảnh bằng mô hình học máy TensorFlow Lite cục bộ để tự động gợi ý danh mục (Food, Drinks, Shopping).
 * 4. Chạy Transaction trong Firestore để ghi giao dịch đồng thời cập nhật trừ số dư ví.
 */
public class CaptureTransactionActivity extends AppCompatActivity {

    private static final String TAG = "CaptureTransaction";
    private ImageView ivCapturedImage;
    private MaterialCardView cardInput;
    private FloatingActionButton fabCapture;
    private MaterialButton btnRetake, btnSave;
    private EditText etTitle, etAmount;
    private View overlay;
    private ProgressBar progressBar;

    private Uri photoUri;
    private File photoFile;
    private String uploadedImageUrl = null;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String IMGBB_API_KEY = "ae3d333d8018f466778a1302f71eb896";
    private com.example.app_quan_li_chi_tieu_ca_nhan.utils.TFLiteClassifier classifier;

    // Bộ yêu cầu cấp quyền chụp ảnh camera từ người dùng
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    // Launcher chụp ảnh từ camera hệ thống
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                Log.d(TAG, "Camera result: " + result);
                if (result && photoFile != null && photoFile.exists() && photoFile.length() > 0) {
                    showCapturedImage();
                } else {
                    Toast.makeText(this, "Không chụp được ảnh hoặc hủy", Toast.LENGTH_SHORT).show();
                    fabCapture.setVisibility(View.VISIBLE);
                    cardInput.setVisibility(View.GONE);
                    overlay.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_capture_transaction);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            
            // Khởi tạo bộ phân loại TFLite
            try {
                classifier = new com.example.app_quan_li_chi_tieu_ca_nhan.utils.TFLiteClassifier(this);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi nạp mô hình TFLite", e);
                Toast.makeText(this, "Không thể khởi động mô hình AI cục bộ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // Ánh xạ UI
            ivCapturedImage = findViewById(R.id.ivCapturedImage);
            cardInput = findViewById(R.id.cardInput);
            fabCapture = findViewById(R.id.fabCapture);
            btnRetake = findViewById(R.id.btnRetake);
            btnSave = findViewById(R.id.btnSave);
            etTitle = findViewById(R.id.etTitle);
            etAmount = findViewById(R.id.etAmount);
            overlay = findViewById(R.id.overlay);
            progressBar = findViewById(R.id.progressBar);

            if (fabCapture != null) fabCapture.setOnClickListener(v -> checkPermissionAndStartCamera());
            if (btnRetake != null) btnRetake.setOnClickListener(v -> checkPermissionAndStartCamera());
            if (btnSave != null) btnSave.setOnClickListener(v -> validateAndSave());
            
            // Tự động kiểm tra quyền và mở camera sau khi giao diện đã sẵn sàng
            ivCapturedImage.post(() -> checkPermissionAndStartCamera());
            
        } catch (Exception e) {
            Log.e(TAG, "Crash in onCreate", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng mô hình TFLite tránh rò rỉ bộ nhớ
        if (classifier != null) {
            classifier.close();
        }
    }

    /**
     * Kiểm tra quyền camera. Nếu đã có thì mở, chưa có thì yêu cầu cấp quyền.
     */
    private void checkPermissionAndStartCamera() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkPermissionAndStartCamera", e);
            Toast.makeText(this, "Lỗi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo file ảnh tạm và khởi chạy camera hệ thống.
     */
    private void startCamera() {
        try {
            photoFile = createImageFile();
            if (photoFile != null) {
                // Tạo Uri an toàn bằng FileProvider tránh FileUriExposedException trên Android 7.0+
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.app_quan_li_chi_tieu_ca_nhan.fileprovider",
                        photoFile);
                takePictureLauncher.launch(photoUri);
            } else {
                Toast.makeText(this, "Không thể tạo file lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error starting camera", ex);
            Toast.makeText(this, "Lỗi mở camera: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo file tạm trong thư mục Picture của app.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Cập nhật UI hiển thị ảnh chụp được và hiện form nhập tên/số tiền.
     */
    private void showCapturedImage() {
        ivCapturedImage.setImageURI(photoUri);
        cardInput.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        fabCapture.setVisibility(View.GONE);
        btnRetake.setVisibility(View.VISIBLE);
    }

    /**
     * Kiểm tra dữ liệu người dùng nhập trước khi thực hiện tải ảnh.
     */
    private void validateAndSave() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Nhập tên chi tiêu");
            return;
        }
        if (amountStr.isEmpty()) {
            etAmount.setError("Nhập số tiền");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            uploadImageToImgBB(title, amount);
        } catch (NumberFormatException e) {
            etAmount.setError("Số tiền không hợp lệ");
        }
    }

    /**
     * Chạy luồng phụ để nén, xử lý xoay ảnh và upload lên ImgBB bằng Retrofit.
     */
    private void uploadImageToImgBB(String title, double amount) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        new Thread(() -> {
            try {
                // Xử lý nén ảnh trong Background Thread tránh đơ Main Thread UI
                byte[] data = processImage(photoUri);
                runOnUiThread(() -> {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), data);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", "receipt.jpg", requestFile);

                    RetrofitClient.getImgBBApi().uploadImage(IMGBB_API_KEY, body).enqueue(new Callback<ImgBBResponse>() {
                        @Override
                        public void onResponse(Call<ImgBBResponse> call, Response<ImgBBResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                uploadedImageUrl = response.body().getData().getUrl();
                                // Sau khi upload lấy URL thành công, chạy mô hình AI nhận diện danh mục rồi lưu
                                getAutoCategoryAndSave(title, amount, data);
                            } else {
                                handleError("Lỗi upload ảnh: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<ImgBBResponse> call, Throwable t) {
                            handleError("Lỗi kết nối ImgBB: " + t.getMessage());
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> handleError("Lỗi xử lý ảnh: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Dùng bộ phân loại TFLiteClassifier phân tích hình ảnh để đề xuất danh mục chi tiêu, sau đó lưu giao dịch.
     */
    private void getAutoCategoryAndSave(String title, double amount, byte[] imageData) {
        Log.d(TAG, "Đang sử dụng mô hình TFLite cục bộ để phân loại...");
        
        String category = "Chụp ảnh"; // Danh mục mặc định nếu AI không nhận diện được
        if (classifier != null) {
            try {
                // Giải mã mảng byte thành Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                if (bitmap != null) {
                    category = classifier.classifyImage(bitmap);
                    Log.d(TAG, "Mô hình TFLite cục bộ trả về danh mục: " + category);
                } else {
                    Log.e(TAG, "Không thể giải mã mảng byte thành Bitmap");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi chạy suy luận mô hình TFLite: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Mô hình TFLite chưa được nạp");
        }
        
        saveToFirestore(title, amount, category);
    }

    /**
     * Đọc ảnh từ URI, xoay ảnh đúng chiều EXIF, nén ảnh 70% lấy mảng bytes.
     */
    private byte[] processImage(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();

        // Xoay ảnh về đúng chiều gốc
        bitmap = rotateImageIfRequired(bitmap, uri);

        // Nén ảnh chất lượng 70
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return baos.toByteArray();
    }

    /**
     * Kiểm tra và xoay ảnh dựa theo thuộc tính EXIF (tránh ảnh bị xoay ngang khi chụp bằng một số camera).
     */
    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws Exception {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = new ExifInterface(input);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        input.close();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: return rotate(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180: return rotate(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270: return rotate(img, 270);
            default: return img;
        }
    }

    private Bitmap rotate(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

    /**
     * Ghi thông tin giao dịch vào Firestore trong 1 Transaction để đảm bảo tính toàn vẹn:
     * - Tạo tài liệu giao dịch mới trong 'transactions'.
     * - Cập nhật giảm trừ số dư ví người dùng tương ứng số tiền trong 'balances'.
     */
    private void saveToFirestore(String title, double amount, String categoryName) {
        String userId = mAuth.getUid();
        if (userId == null) {
            handleError("Lỗi: Người dùng chưa đăng nhập");
            return;
        }

        db.runTransaction(transaction -> {
            DocumentReference balanceRef = db.collection("balances").document(userId);
            DocumentReference txRef = db.collection("transactions").document();

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("title", title);
            data.put("amount", amount);
            data.put("categoryName", categoryName);
            data.put("imageUrl", uploadedImageUrl);
            data.put("timestamp", System.currentTimeMillis());
            data.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
            data.put("isExpense", true); // Mặc định qua quét ảnh là chi tiêu

            // Đọc dữ liệu số dư hiện tại
            com.google.firebase.firestore.DocumentSnapshot balanceSnapshot = transaction.get(balanceRef);

            // Ghi dữ liệu giao dịch
            transaction.set(txRef, data);
            
            if (balanceSnapshot.exists()) {
                // Trừ số dư
                transaction.update(balanceRef, "currentBalance", FieldValue.increment(-amount));
            } else {
                // Nếu chưa có balance, tạo mới số dư âm
                Map<String, Object> initialBalance = new HashMap<>();
                initialBalance.put("userId", userId);
                initialBalance.put("currentBalance", -amount);
                initialBalance.put("lastUpdated", System.currentTimeMillis());
                initialBalance.put("currency", "VND");
                transaction.set(balanceRef, initialBalance);
            }
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Đã lưu giao dịch (" + categoryName + ")!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            handleError("Lỗi Firestore: " + e.getMessage());
        });
    }

    private void handleError(String message) {
        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
