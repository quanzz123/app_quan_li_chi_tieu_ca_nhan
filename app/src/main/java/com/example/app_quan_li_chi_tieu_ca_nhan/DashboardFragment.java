package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.adapters.ServiceAdapter;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.ServiceItem;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Balance;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {

    private RecyclerView rvDashboardServices;
    private ServiceAdapter adapter;
    private TextView tvBalanceAmount;
    private android.widget.LinearLayout lnChartContainer;
    private TextView tvNoData;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);
        
        // Cấu hình các View cho biểu đồ thống kê
        lnChartContainer = view.findViewById(R.id.lnChartContainer);
        tvNoData = view.findViewById(R.id.tvNoData);

        rvDashboardServices = view.findViewById(R.id.rvDashboardServices);
        rvDashboardServices.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new ServiceAdapter(getDummyServices(), service -> {
            Intent intent = new Intent(getActivity(), AddActivity.class);
            intent.putExtra("categoryName", service.getName());
            startActivity(intent);
        });
        rvDashboardServices.setAdapter(adapter);

        view.findViewById(R.id.btnTopup).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TopupActivity.class));
        });

        view.findViewById(R.id.tvMoreStatistics).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdvancedStatisticsActivity.class));
        });

        view.findViewById(R.id.btnTransfer).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TopupHistoryActivity.class));
        });

        view.findViewById(R.id.tvHistoryText).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TopupHistoryActivity.class));
        });

        listenForBalance();
        listenForTransactions();

        return view;
    }

    private void listenForBalance() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("balances").document(userId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        Balance balance = value.toObject(Balance.class);
                        if (balance != null) {
                            tvBalanceAmount.setText(CurrencyUtils.formatVND(balance.getCurrentBalance()));
                        }
                    }
                });
    }

    private void listenForTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("DashboardFragment", "Lỗi tải giao dịch: ", error);
                        return;
                    }
                    if (value != null) {
                        List<com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction> transactions = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction tx = doc.toObject(com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction.class);
                            if (tx != null) {
                                transactions.add(tx);
                            }
                        }
                        updateChart(transactions);
                    }
                });
    }

    private void updateChart(List<com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction> transactions) {
        if (lnChartContainer == null) return;
        lnChartContainer.removeAllViews();

        // 1. Phân nhóm chi tiêu theo danh mục (chỉ thống kê giao dịch là chi tiêu: isExpense = true)
        java.util.Map<String, Double> categoryExpenses = new java.util.HashMap<>();
        for (com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction tx : transactions) {
            if (tx.isExpense()) {
                String category = tx.getCategoryName();
                if (category == null || category.trim().isEmpty()) {
                    category = "Khác";
                }
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + tx.getAmount());
            }
        }

        if (categoryExpenses.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            return;
        } else {
            tvNoData.setVisibility(View.GONE);
        }

        // 2. Tìm giá trị chi tiêu lớn nhất để làm mốc tỷ lệ chiều cao của cột
        double maxExpense = 0;
        for (double amt : categoryExpenses.values()) {
            if (amt > maxExpense) {
                maxExpense = amt;
            }
        }

        // 3. Định nghĩa bảng màu sắc hiện đại cho từng danh mục
        java.util.Map<String, String> categoryColors = new java.util.HashMap<>();
        categoryColors.put("Drinks", "#3B82F6");      // Xanh dương
        categoryColors.put("Food", "#F59E0B");        // Cam/Vàng
        categoryColors.put("Shopping", "#10B981");    // Xanh lá
        categoryColors.put("Chụp ảnh", "#8B5CF6");    // Tím
        categoryColors.put("Khác", "#64748B");        // Xám

        // 4. Vẽ cột cho từng danh mục
        float density = getResources().getDisplayMetrics().density;
        int maxBarHeightPx = Math.round(130 * density); // Chiều cao tối đa của cột là 130dp
        int barWidthPx = Math.round(32 * density);      // Chiều rộng cột 32dp

        for (java.util.Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();

            // Tính chiều cao cột tỉ lệ thuận với số tiền chi tiêu
            int barHeightPx = maxExpense > 0 ? (int) ((amount / maxExpense) * maxBarHeightPx) : 0;
            if (barHeightPx < Math.round(10 * density)) {
                barHeightPx = Math.round(10 * density); // Chiều cao tối thiểu 10dp để cột vẫn hiển thị
            }

            // Tạo layout dọc cho từng cột biểu đồ
            android.widget.LinearLayout colLayout = new android.widget.LinearLayout(getContext());
            colLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
            colLayout.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            
            android.widget.LinearLayout.LayoutParams colParams = new android.widget.LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            colLayout.setLayoutParams(colParams);

            // Text hiển thị số tiền rút gọn trên đỉnh cột (Ví dụ: 150K)
            TextView tvAmount = new TextView(getContext());
            String shortAmountText;
            if (amount >= 1000000) {
                shortAmountText = String.format(java.util.Locale.getDefault(), "%.1fM", amount / 1000000.0);
            } else if (amount >= 1000) {
                shortAmountText = String.format(java.util.Locale.getDefault(), "%.0fK", amount / 1000.0);
            } else {
                shortAmountText = String.format(java.util.Locale.getDefault(), "%.0f", amount);
            }
            tvAmount.setText(shortAmountText);
            tvAmount.setTextSize(10);
            tvAmount.setTextColor(android.graphics.Color.parseColor("#64748B"));
            tvAmount.setGravity(android.view.Gravity.CENTER);
            
            android.widget.LinearLayout.LayoutParams amountParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            amountParams.setMargins(0, 0, 0, Math.round(4 * density));
            tvAmount.setLayoutParams(amountParams);
            colLayout.addView(tvAmount);

            // View thể hiện cột màu
            View barView = new View(getContext());
            android.widget.LinearLayout.LayoutParams barParams = new android.widget.LinearLayout.LayoutParams(
                    barWidthPx, barHeightPx);
            barView.setLayoutParams(barParams);

            // Thiết lập bo góc trên và màu nền cho cột
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            String colorHex = categoryColors.getOrDefault(category, "#2563EB");
            drawable.setColor(android.graphics.Color.parseColor(colorHex));
            drawable.setCornerRadii(new float[]{
                    Math.round(6 * density), Math.round(6 * density), // top-left bo tròn
                    Math.round(6 * density), Math.round(6 * density), // top-right bo tròn
                    0, 0, // bottom-right
                    0, 0  // bottom-left
            });
            barView.setBackground(drawable);
            colLayout.addView(barView);

            // Text danh mục ở chân cột
            TextView tvCategory = new TextView(getContext());
            tvCategory.setText(category);
            tvCategory.setTextSize(11);
            tvCategory.setTextColor(android.graphics.Color.parseColor("#0F172A"));
            tvCategory.setGravity(android.view.Gravity.CENTER);
            
            android.widget.LinearLayout.LayoutParams categoryParams = new android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            categoryParams.setMargins(0, Math.round(6 * density), 0, 0);
            tvCategory.setLayoutParams(categoryParams);
            colLayout.addView(tvCategory);

            lnChartContainer.addView(colLayout);
        }
    }

    private List<ServiceItem> getDummyServices() {
        List<ServiceItem> list = new ArrayList<>();
        list.add(new ServiceItem("Drinks", R.drawable.drink_service_icon));
        list.add(new ServiceItem("Food", R.drawable.food_service_icon));
        list.add(new ServiceItem("Shopping", R.drawable.shopping_service_icon));

        return list;
    }
}
