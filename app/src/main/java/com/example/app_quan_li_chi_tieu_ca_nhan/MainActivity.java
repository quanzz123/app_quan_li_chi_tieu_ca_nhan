package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navDashboard, navNotifications, navProfile;
    private final int COLOR_ACTIVE = Color.parseColor("#2563EB");
    private final int COLOR_INACTIVE = Color.parseColor("#64748B");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Đảm bảo Firebase được khởi tạo
        if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(this);
        }

        // Kiểm tra nếu chưa đăng nhập thì chuyển sang LoginActivity
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Xử lý System Insets để tránh bị đè bởi navigation bar của thiết bị
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigationContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initViews();
        setupNavigation();

        // Kiểm tra xem có yêu cầu mở tab Dashboard không
        String selectTab = getIntent().getStringExtra("select_tab");
        if ("dashboard".equals(selectTab)) {
            loadFragment(new DashboardFragment(), navDashboard);
        } else {
            // Mặc định load màn hình Home (Thanh toán - Ảnh 1)
            loadFragment(new HomePaymentFragment(), navHome);
        }

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            // Mở màn hình chụp ảnh giao dịch
            Intent intent = new Intent(this, CaptureTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navDashboard = findViewById(R.id.navDashboard);
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> loadFragment(new HomePaymentFragment(), navHome));
        navDashboard.setOnClickListener(v -> loadFragment(new DashboardFragment(), navDashboard));
        navNotifications.setOnClickListener(v -> loadFragment(new TransactionsFragment(), navNotifications));
        navProfile.setOnClickListener(v -> loadFragment(new ProfileFragment(), navProfile));
    }

    private void loadFragment(Fragment fragment, ImageView activeNav) {
        // Cập nhật màu sắc các icon điều hướng
        resetNavColors();
        activeNav.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));

        // Thực hiện chuyển đổi Fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void resetNavColors() {
        navHome.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        navDashboard.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        navNotifications.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        navProfile.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String selectTab = intent.getStringExtra("select_tab");
        if ("dashboard".equals(selectTab)) {
            loadFragment(new DashboardFragment(), navDashboard);
        }
    }
}
