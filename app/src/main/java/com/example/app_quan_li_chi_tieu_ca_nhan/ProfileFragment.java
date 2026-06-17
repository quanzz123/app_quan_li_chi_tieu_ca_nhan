package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app_quan_li_chi_tieu_ca_nhan.models.UserProfile;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Fragment hiển thị thông tin hồ sơ người dùng (Profile).
 * Cho phép xem chi tiết thông tin cá nhân, mục tiêu tài chính (thu nhập, tiết kiệm)
 * và cung cấp tính năng chuyển hướng chỉnh sửa thông tin hoặc đăng xuất.
 */
public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvUserBio;
    private TextView tvUserPhone, tvUserDOB, tvUserGender, tvUserAddress, tvUserOccupation;
    private TextView tvIncomeGoal, tvSavingGoal;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DecimalFormat currencyFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Khởi tạo định dạng tiền tệ riêng để hiển thị mục tiêu tài chính
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        currencyFormatter = new DecimalFormat("#,###", symbols);

        // Ánh xạ các trường Text trên UI
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        tvUserDOB = view.findViewById(R.id.tvUserDOB);
        tvUserGender = view.findViewById(R.id.tvUserGender);
        tvUserAddress = view.findViewById(R.id.tvUserAddress);
        tvUserOccupation = view.findViewById(R.id.tvUserOccupation);
        
        tvIncomeGoal = view.findViewById(R.id.tvIncomeGoal);
        tvSavingGoal = view.findViewById(R.id.tvSavingGoal);

        // Xử lý sự kiện nhấn nút Chỉnh sửa hồ sơ
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Xử lý sự kiện nhấn nút Đăng xuất tài khoản
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut(); // Đăng xuất khỏi Firebase Auth
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mỗi khi quay lại fragment (ví dụ: sau khi chỉnh sửa), tải lại dữ liệu mới nhất
        loadUserData();
    }

    /**
     * Tải thông tin người dùng từ cả hai Collection trên Firestore:
     * - 'users': chứa thông tin tài khoản cơ bản (fullName).
     * - 'profiles': chứa thông tin chi tiết mở rộng (bio, phone, mục tiêu tài chính).
     */
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        tvUserEmail.setText(currentUser.getEmail());

        // 1. Lấy thông tin cơ bản của người dùng (fullName) từ Collection 'users'
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            tvUserName.setText(fullName);
                        } else {
                            tvUserName.setText("Chưa đặt tên");
                        }
                    } else {
                        tvUserName.setText("Người dùng");
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải thông tin cơ bản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // 2. Lấy thông tin chi tiết mở rộng từ Collection 'profiles'
        db.collection("profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            updateProfileUI(profile);
                            return;
                        }
                    }
                    // Nếu tài liệu profile chưa được tạo, hiển thị giá trị trống mặc định
                    showPlaceholderProfile();
                })
                .addOnFailureListener(e -> {
                    showPlaceholderProfile();
                });
    }

    /**
     * Cập nhật các trường giao diện bằng dữ liệu thực tế từ đối tượng UserProfile.
     */
    private void updateProfileUI(UserProfile profile) {
        // Tiểu sử (Bio)
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            tvUserBio.setText(profile.getBio());
            tvUserBio.setAlpha(1.0f);
        } else {
            tvUserBio.setText("Chưa có giới thiệu bản thân");
            tvUserBio.setAlpha(0.6f);
        }

        // Số điện thoại
        if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
            tvUserPhone.setText(profile.getPhoneNumber());
        } else {
            tvUserPhone.setText("Chưa thiết lập");
        }

        // Ngày sinh
        if (profile.getDateOfBirth() != null && !profile.getDateOfBirth().isEmpty()) {
            tvUserDOB.setText(profile.getDateOfBirth());
        } else {
            tvUserDOB.setText("Chưa thiết lập");
        }

        // Giới tính
        if (profile.getGender() != null && !profile.getGender().isEmpty()) {
            tvUserGender.setText(profile.getGender());
        } else {
            tvUserGender.setText("Chưa thiết lập");
        }

        // Địa chỉ
        if (profile.getAddress() != null && !profile.getAddress().isEmpty()) {
            tvUserAddress.setText(profile.getAddress());
        } else {
            tvUserAddress.setText("Chưa thiết lập");
        }

        // Nghề nghiệp
        if (profile.getOccupation() != null && !profile.getOccupation().isEmpty()) {
            tvUserOccupation.setText(profile.getOccupation());
        } else {
            tvUserOccupation.setText("Chưa thiết lập");
        }

        // Các mục tiêu tài chính
        tvIncomeGoal.setText(formatCurrency(profile.getMonthlyIncomeGoal()));
        tvSavingGoal.setText(formatCurrency(profile.getSavingGoal()));
    }

    /**
     * Hiển thị giao diện mặc định khi không tìm thấy tài liệu Profile của người dùng.
     */
    private void showPlaceholderProfile() {
        tvUserBio.setText("Chưa có giới thiệu bản thân");
        tvUserBio.setAlpha(0.6f);
        tvUserPhone.setText("Chưa thiết lập");
        tvUserDOB.setText("Chưa thiết lập");
        tvUserGender.setText("Chưa thiết lập");
        tvUserAddress.setText("Chưa thiết lập");
        tvUserOccupation.setText("Chưa thiết lập");
        tvIncomeGoal.setText("0 đ");
        tvSavingGoal.setText("0 đ");
    }

    /**
     * Hàm định dạng tiền tệ VND nội bộ.
     */
    private String formatCurrency(double amount) {
        try {
            return currencyFormatter.format(amount) + " đ";
        } catch (Exception e) {
            return (int) amount + " đ";
        }
    }
}