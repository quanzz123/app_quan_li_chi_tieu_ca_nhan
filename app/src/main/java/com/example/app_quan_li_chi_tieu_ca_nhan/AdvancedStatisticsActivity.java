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

/**
 * Activity hiển thị báo cáo thống kê chi tiết (Advanced Statistics).
 * Các chức năng chính:
 * 1. Lọc giao dịch theo các khoảng thời gian: Hôm nay, Tuần này, Tháng này, Tất cả.
 * 2. Phân tích thống kê tổng thu nhập và tổng chi tiêu theo khoảng thời gian được chọn.
 * 3. Vẽ biểu đồ phân rã danh mục chi tiêu (Category Breakdown) bằng thanh tiến trình tự thiết kế động.
 * 4. Liệt kê danh sách các giao dịch thuộc bộ lọc.
 */
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

    // Định nghĩa ID tương ứng các nút lọc thời gian dạng Chip
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

    /**
     * Tải danh sách tất cả giao dịch của người dùng hiện tại từ Firestore.
     */
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
                        // Sau khi load xong toàn bộ dữ liệu, áp dụng bộ lọc hiện đang chọn
                        applyFilter(cgTimeFilter.getCheckedChipId());
                    }
                });
    }

    /**
     * Áp dụng bộ lọc thời gian để lọc giao dịch, tổng hợp doanh thu/chi tiêu,
     * vẽ biểu đồ cột tỷ lệ và cập nhật RecyclerView.
     *
     * @param checkedChipId ID của Chip thời gian đang chọn
     */
    private void applyFilter(int checkedChipId) {
        filteredTransactions.clear();

        long now = System.currentTimeMillis();
        long filterStartTime = 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Xác định mốc thời gian tối thiểu của bộ lọc
        if (checkedChipId == FILTER_TODAY) {
            filterStartTime = cal.getTimeInMillis();
        } else if (checkedChipId == FILTER_WEEK) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            filterStartTime = cal.getTimeInMillis();
        } else if (checkedChipId == FILTER_MONTH) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            filterStartTime = cal.getTimeInMillis();
        } else {
            // FILTER_ALL: không giới hạn
            filterStartTime = 0;
        }

        double totalExpense = 0;
        double totalIncome = 0;
        Map<String, Double> categoryExpenses = new HashMap<>();

        // Sử dụng Helper SimpleDateFormat để parse chuỗi ngày "dd/MM/yyyy" thành timestamp để so sánh
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

            // Đối chiếu xem giao dịch có nằm trong mốc thời gian lọc hay không
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

        // Cập nhật danh sách giao dịch hiển thị
        adapter.notifyDataSetChanged();
        if (filteredTransactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvFilteredTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvFilteredTransactions.setVisibility(View.VISIBLE);
        }

        // Dựng lại biểu đồ phân rã phần trăm danh mục chi tiêu (Category Breakdown)
        updateCategoryBreakdown(categoryExpenses, totalExpense);
    }

    /**
     * Dựng giao diện các thanh phần trăm chi tiêu cho từng danh mục một cách động.
     *
     * @param categoryExpenses Map chứa tên danh mục và tổng chi tiêu của danh mục đó
     * @param totalExpense Tổng toàn bộ chi tiêu thuộc bộ lọc
     */
    private void updateCategoryBreakdown(Map<String, Double> categoryExpenses, double totalExpense) {
        lnCategoryBreakdown.removeAllViews(); // Xóa sạch giao diện cũ trước khi vẽ lại

        if (categoryExpenses.isEmpty() || totalExpense <= 0) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            return;
        } else {
            tvNoCategoryData.setVisibility(View.GONE);
        }

        // Bảng màu cố định cho các danh mục tiêu biểu
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

            // 2. Tạo Layout ngang chứa Tên danh mục và Số tiền + % tương ứng bên phải
            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLayout.setLayoutParams(textLayoutParams);

            // Text tên danh mục
            TextView tvCatName = new TextView(this);
            tvCatName.setText(category);
            tvCatName.setTextColor(Color.parseColor("#0F172A"));
            tvCatName.setTextSize(14);
            tvCatName.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams catNameParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            tvCatName.setLayoutParams(catNameParams);
            textLayout.addView(tvCatName);

            // Text giá trị tiền tệ + % (ví dụ: "50.000 đ (25%)")
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

            // 3. Tạo thanh tiến trình ngang tự chế bằng cách xếp chồng các View con
            // Tạo thanh nền màu xám nhạt (Track)
            LinearLayout trackView = new LinearLayout(this);
            trackView.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams trackParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.round(8 * density));
            trackParams.setMargins(0, Math.round(8 * density), 0, 0);
            trackView.setLayoutParams(trackParams);

            // Thiết lập bo góc 4dp và màu xám cho thanh nền
            GradientDrawable trackBg = new GradientDrawable();
            trackBg.setColor(Color.parseColor("#F1F5F9"));
            trackBg.setCornerRadius(Math.round(4 * density));
            trackView.setBackground(trackBg);

            // Tạo phần thanh tiến trình có chiều rộng chiếm tỷ lệ phần trăm tương ứng
            View progressView = new View(this);
            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, (float) (percentage / 100.0));
            progressView.setLayoutParams(progressParams);

            // Thiết lập màu sắc và bo góc cho thanh tiến trình
            GradientDrawable progressBg = new GradientDrawable();
            String colorHex = categoryColors.getOrDefault(category, "#2563EB");
            progressBg.setColor(Color.parseColor(colorHex));
            progressBg.setCornerRadius(Math.round(4 * density));
            progressView.setBackground(progressBg);
            trackView.addView(progressView);

            // Tạo spacer trống chiếm khoảng trống còn lại bên phải (100% - percentage)
            View spacerView = new View(this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, (float) ((100.0 - percentage) / 100.0));
            spacerView.setLayoutParams(spacerParams);
            trackView.addView(spacerView);

            itemLayout.addView(trackView);

            // Add dòng thống kê danh mục hoàn thiện vào view container
            lnCategoryBreakdown.addView(itemLayout);
        }
    }
}
