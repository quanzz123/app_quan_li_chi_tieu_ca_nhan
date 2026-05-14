package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Firebase một lần duy nhất cho toàn bộ ứng dụng
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully in Application class");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
        }
    }
}
