package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

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
            Toast.makeText(this, "Đã lưu giao dịch mẫu", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
