package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.adapters.TransactionAdapter;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedStatisticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ChipGroup cgTimeFilter;
    private TextView tvTotalExpense, tvTotalIncome, tvNoCategoryData, tvNoTransactions;
    private LinearLayout lnCategoryBreakdown;
    private RecyclerView rvFilteredTransactions;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();
    private TransactionAdapter adapter;

    private static final int FILTER_TODAY = R.id.chipToday;
    private static final int FILTER_WEEK = R.id.chipWeek;
    private static final int FILTER_MONTH = R.id.chipMonth;
    private static final int FILTER_ALL = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_statistics);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        loadTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cgTimeFilter = findViewById(R.id.cgTimeFilter);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
        lnCategoryBreakdown = findViewById(R.id.lnCategoryBreakdown);
        rvFilteredTransactions = findViewById(R.id.rvFilteredTransactions);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvFilteredTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(filteredTransactions, transaction -> {
            Intent intent = new Intent(AdvancedStatisticsActivity.this, TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            startActivity(intent);
        });
        rvFilteredTransactions.setAdapter(adapter);
    }

    private void setupFilters() {
        cgTimeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                applyFilter(checkedIds.get(0));
            }
        });
    }

    private void loadTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        allTransactions.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Transaction tx = doc.toObject(Transaction.class);
                            if (tx != null) {
                                allTransactions.add(tx);
                            }
                        }
                        // Sau khi load xong dữ liệu, áp dụng bộ lọc hiện tại
                        applyFilter(cgTimeFilter.getCheckedChipId());
                    }
                });
    }

    private void applyFilter(int checkedChipId) {
        filteredTransactions.clear();

        long now = System.currentTimeMillis();
        long filterStartTime = 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (checkedChipId == FILTER_TODAY) {
            filterStartTime = cal.getTimeInMillis();
        } else if (checkedChipId == FILTER_WEEK) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            filterStartTime = cal.getTimeInMillis();
        } else if (checkedChipId == FILTER_MONTH) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            filterStartTime = cal.getTimeInMillis();
        } else {
            // FILTER_ALL
            filterStartTime = 0;
        }

        double totalExpense = 0;
        double totalIncome = 0;
        Map<String, Double> categoryExpenses = new HashMap<>();

        // Duyệt qua tất cả giao dịch để lọc
        for (Transaction tx : allTransactions) {
            // Firestore lưu timestamp là trường "timestamp" (Long)
            // Lấy ra timestamp của giao dịch. Nếu bằng 0 thì lấy System.currentTimeMillis() tạm
            long txTimestamp = now;
            try {
                // Kiểm tra trực tiếp vì Firebase map qua toObject
                // Nếu data cũ không có timestamp thì có thể xem xét qua date
            } catch (Exception e) {}

            // Giả định đối tượng Transaction có lưu trữ timestamp hoặc chúng ta query được từ doc.
            // Để an toàn, chúng ta lấy timestamp đã có trong model (nếu có trường này).
            // Hãy kiểm tra trong model Transaction.java xem có timestamp không.
            // Đợi đã, lúc nãy chúng ta xem Transaction.java:
            // Nó chỉ có: title, categoryName, date, amount, iconRes, isExpense, imageUrl.
            // Ồ! Transaction.java không có trường timestamp trong model!
            // Nhưng trong Firestore lúc lưu chúng ta thấy: txData.put("timestamp", System.currentTimeMillis());
            // Vì vậy, Transaction.java có thể không ánh xạ trường này do thiếu getter/setter,
            // hoặc nó thực chất được lưu trên Firestore nhưng Class Transaction không có field này.
            // Khoan đã! Hãy mở lại code lưu giao dịch lúc nãy:
            // "txData.put("timestamp", System.currentTimeMillis());"
            // Và trong TransactionDetailActivity.java nó chỉ hiển thị ngày (date).
            // Nếu Transaction.java không có field timestamp, làm sao chúng ta lọc theo thời gian?
            // Chúng ta có thể lọc dựa vào trường `date` (định dạng dd/MM/yyyy).
            // Hoặc chúng ta có thể parse chuỗi ngày `date` thành Date để đối chiếu.
            // Điều này rất an toàn vì định dạng ngày luôn là "dd/MM/yyyy".
        }

        // Hãy viết helper để chuyển đổi chuỗi "dd/MM/yyyy" thành timestamp để lọc:
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

        for (Transaction tx : allTransactions) {
            long txTime = 0;
            try {
                if (tx.getDate() != null && !tx.getDate().isEmpty()) {
                    java.util.Date parsedDate = sdf.parse(tx.getDate());
                    if (parsedDate != null) {
                        txTime = parsedDate.getTime();
                    }
                }
            } catch (Exception e) {
                txTime = now;
            }

            // Đối chiếu thời gian lọc
            if (checkedChipId == FILTER_ALL || txTime >= filterStartTime) {
                filteredTransactions.add(tx);

                if (tx.isExpense()) {
                    totalExpense += tx.getAmount();
                    String category = tx.getCategoryName();
                    if (category == null || category.trim().isEmpty()) {
                        category = "Khác";
                    }
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + tx.getAmount());
                } else {
                    totalIncome += tx.getAmount();
                }
            }
        }

        // Cập nhật giao diện tổng quan Thu/Chi
        tvTotalExpense.setText(CurrencyUtils.formatVND(totalExpense));
        tvTotalIncome.setText(CurrencyUtils.formatVND(totalIncome));

        // Cập nhật danh sách giao dịch
        adapter.notifyDataSetChanged();
        if (filteredTransactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvFilteredTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvFilteredTransactions.setVisibility(View.VISIBLE);
        }

        // Cập nhật biểu đồ danh mục (Category Breakdown)
        updateCategoryBreakdown(categoryExpenses, totalExpense);
    }

    private void updateCategoryBreakdown(Map<String, Double> categoryExpenses, double totalExpense) {
        lnCategoryBreakdown.removeAllViews();

        if (categoryExpenses.isEmpty() || totalExpense <= 0) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            return;
        } else {
            tvNoCategoryData.setVisibility(View.GONE);
        }

        // Định nghĩa bảng màu sắc danh mục
        Map<String, String> categoryColors = new HashMap<>();
        categoryColors.put("Drinks", "#3B82F6");      // Xanh dương
        categoryColors.put("Food", "#F59E0B");        // Cam/Vàng
        categoryColors.put("Shopping", "#10B981");    // Xanh lá
        categoryColors.put("Chụp ảnh", "#8B5CF6");    // Tím
        categoryColors.put("Khác", "#64748B");        // Xám

        float density = getResources().getDisplayMetrics().density;

        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double percentage = (amount / totalExpense) * 100.0;

            // 1. Tạo container layout dọc cho mỗi dòng danh mục
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            itemParams.setMargins(0, 0, 0, Math.round(16 * density));
            itemLayout.setLayoutParams(itemParams);

            // 2. Tạo Layout ngang chứa Tên danh mục và Số tiền + %
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLayout.setLayoutParams(textLayoutParams);

            // Tên danh mục
            TextView tvCatName = new TextView(this);
            tvCatName.setText(category);
            tvCatName.setTextColor(Color.parseColor("#0F172A"));
            tvCatName.setTextSize(14);
            tvCatName.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams catNameParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvCatName.setLayoutParams(catNameParams);
            textLayout.addView(tvCatName);

            // Số tiền và phần trăm
            TextView tvCatValue = new TextView(this);
            String valueText = String.format(java.util.Locale.getDefault(), "%s (%.1f%%)", 
                    CurrencyUtils.formatVND(amount), percentage);
            tvCatValue.setText(valueText);
            tvCatValue.setTextColor(Color.parseColor("#475569"));
            tvCatValue.setTextSize(13);
            tvCatValue.setGravity(Gravity.END);
            LinearLayout.LayoutParams catValueParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvCatValue.setLayoutParams(catValueParams);
            textLayout.addView(tvCatValue);

            itemLayout.addView(textLayout);

            // 3. Tạo thanh ProgressBar ngang tự chế bằng CardView/Frame/Views
            // Lớp nền (Track)
            LinearLayout trackView = new LinearLayout(this);
            trackView.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.round(8 * density));
            trackParams.setMargins(0, Math.round(8 * density), 0, 0);
            trackView.setLayoutParams(trackParams);

            // Thiết lập bo góc và màu xám nhạt cho track
            GradientDrawable trackBg = new GradientDrawable();
            trackBg.setColor(Color.parseColor("#F1F5F9"));
            trackBg.setCornerRadius(Math.round(4 * density));
            trackView.setBackground(trackBg);

            // Phần thanh hiển thị tiến trình (Progress Bar)
            View progressView = new View(this);
            // Chiều rộng tính theo phần trăm
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, (float) (percentage / 100.0));
            progressView.setLayoutParams(progressParams);

            // Màu của danh mục
            GradientDrawable progressBg = new GradientDrawable();
            String colorHex = categoryColors.getOrDefault(category, "#2563EB");
            progressBg.setColor(Color.parseColor(colorHex));
            progressBg.setCornerRadius(Math.round(4 * density));
            progressView.setBackground(progressBg);
            trackView.addView(progressView);

            // Spacer đệm cho phần trống còn lại bên phải để không bị tràn cột
            View spacerView = new View(this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, (float) ((100.0 - percentage) / 100.0));
            spacerView.setLayoutParams(spacerParams);
            trackView.addView(spacerView);

            itemLayout.addView(trackView);

            lnCategoryBreakdown.addView(itemLayout);
        }
    }
}
