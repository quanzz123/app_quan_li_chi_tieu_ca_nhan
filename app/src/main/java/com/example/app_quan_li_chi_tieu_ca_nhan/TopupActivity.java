package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TopupActivity extends AppCompatActivity {

    private TextInputEditText etTopupAmount;
    private ChipGroup cgQuickAmounts;
    private MaterialButton btnConfirmTopup;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupQuickSelection();

        btnConfirmTopup.setOnClickListener(v -> performTopup());
    }

    private void initViews() {
        etTopupAmount = findViewById(R.id.etTopupAmount);
        cgQuickAmounts = findViewById(R.id.cgQuickAmounts);
        btnConfirmTopup = findViewById(R.id.btnConfirmTopup);
        progressBar = findViewById(R.id.topupProgressBar);
    }

    private void setupQuickSelection() {
        cgQuickAmounts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                etTopupAmount.setText(chip.getText().toString());
            }
        });
    }

    private void performTopup() {
        String amountStr = etTopupAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            etTopupAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Lỗi: Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnConfirmTopup.setEnabled(false);

        db.runTransaction(transaction -> {
            DocumentReference balanceRef = db.collection("balances").document(userId);
            
            // Cập nhật số dư (Cộng thêm)
            transaction.update(balanceRef, "currentBalance", FieldValue.increment(amount));
            transaction.update(balanceRef, "lastUpdated", System.currentTimeMillis());

            // Có thể thêm một giao dịch loại "Topup" vào collection transactions nếu muốn theo dõi lịch sử nạp
            DocumentReference txRef = db.collection("transactions").document();
            Map<String, Object> txData = new HashMap<>();
            txData.put("userId", userId);
            txData.put("title", "Nạp tiền vào tài khoản");
            txData.put("categoryName", "Topup");
            txData.put("amount", amount);
            txData.put("date", "Hôm nay"); // Có thể dùng DateFormat để lấy ngày thực
            txData.put("timestamp", System.currentTimeMillis());
            txData.put("isExpense", false);
            transaction.set(txRef, txData);

            return null;
        }).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(TopupActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            btnConfirmTopup.setEnabled(true);
            if (e.getMessage() != null && e.getMessage().contains("NOT_FOUND")) {
                initializeBalanceAndRetry(userId, amount);
            } else {
                Toast.makeText(TopupActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeBalanceAndRetry(String userId, double amount) {
        Map<String, Object> initialBalance = new HashMap<>();
        initialBalance.put("userId", userId);
        initialBalance.put("currentBalance", amount);
        initialBalance.put("lastUpdated", System.currentTimeMillis());
        initialBalance.put("currency", "VND");

        db.collection("balances").document(userId)
                .set(initialBalance)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(TopupActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirmTopup.setEnabled(true);
                    Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
