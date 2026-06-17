package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TopupHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ChipGroup cgPeriodFilter;
    private TextView tvNoHistoryData;
    private RecyclerView rvTopupHistory;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private List<Transaction> topupTransactions = new ArrayList<>();
    private List<HistoryGroup> groupList = new ArrayList<>();
    private TopupHistoryGroupAdapter adapter;

    private static final int FILTER_DAY = R.id.chipByDay;
    private static final int FILTER_MONTH = R.id.chipByMonth;
    private static final int FILTER_YEAR = R.id.chipByYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        loadTopupTransactions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cgPeriodFilter = findViewById(R.id.cgPeriodFilter);
        tvNoHistoryData = findViewById(R.id.tvNoHistoryData);
        rvTopupHistory = findViewById(R.id.rvTopupHistory);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Nhấn nút back trên toolbar sẽ kết thúc Activity quay lại Dashboard
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvTopupHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TopupHistoryGroupAdapter(groupList);
        rvTopupHistory.setAdapter(adapter);
    }

    private void setupFilters() {
        cgPeriodFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                groupData(checkedIds.get(0));
            }
        });
    }

    private void loadTopupTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tái sử dụng index (userId, timestamp) đã có trên Firestore
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải lịch sử: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        topupTransactions.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Transaction tx = doc.toObject(Transaction.class);
                            if (tx != null && !tx.isExpense()) {
                                topupTransactions.add(tx);
                            }
                        }
                        // Sau khi load xong, gom nhóm dữ liệu theo bộ lọc hiện tại
                        groupData(cgPeriodFilter.getCheckedChipId());
                    }
                });
    }

    private void groupData(int checkedChipId) {
        groupList.clear();

        if (topupTransactions.isEmpty()) {
            tvNoHistoryData.setVisibility(View.VISIBLE);
            rvTopupHistory.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        Map<String, HistoryGroup> map = new LinkedHashMap<>(); // Duy trì thứ tự chèn

        for (Transaction tx : topupTransactions) {
            String rawDate = tx.getDate();
            if (rawDate == null || rawDate.trim().isEmpty() || rawDate.equals("Hôm nay")) {
                // Fallback nếu date trống hoặc có chữ "Hôm nay" từ bản ghi cũ
                rawDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            }

            String groupKey = "";

            if (checkedChipId == FILTER_DAY) {
                // Giữ nguyên dd/MM/yyyy
                groupKey = rawDate;
            } else if (checkedChipId == FILTER_MONTH) {
                // Lấy MM/yyyy từ vị trí thứ 3 (dd/MM/yyyy -> MM/yyyy)
                if (rawDate.length() >= 10) {
                    groupKey = "Tháng " + rawDate.substring(3);
                } else {
                    groupKey = "Tháng " + new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(new Date());
                }
            } else if (checkedChipId == FILTER_YEAR) {
                // Lấy yyyy từ vị trí thứ 6 (dd/MM/yyyy -> yyyy)
                if (rawDate.length() >= 10) {
                    groupKey = "Năm " + rawDate.substring(6);
                } else {
                    groupKey = "Năm " + new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
                }
            }

            if (map.containsKey(groupKey)) {
                map.get(groupKey).amount += tx.getAmount();
            } else {
                HistoryGroup groupItem = new HistoryGroup(groupKey, tx.getAmount());
                map.put(groupKey, groupItem);
                groupList.add(groupItem);
            }
        }

        adapter.notifyDataSetChanged();

        if (groupList.isEmpty()) {
            tvNoHistoryData.setVisibility(View.VISIBLE);
            rvTopupHistory.setVisibility(View.GONE);
        } else {
            tvNoHistoryData.setVisibility(View.GONE);
            rvTopupHistory.setVisibility(View.VISIBLE);
        }
    }

    // Model phụ trợ cho danh sách gom nhóm
    public static class HistoryGroup {
        public String title;
        public double amount;

        public HistoryGroup(String title, double amount) {
            this.title = title;
            this.amount = amount;
        }
    }

    // Adapter RecyclerView cho nhóm thống kê nạp tiền
    private static class TopupHistoryGroupAdapter extends RecyclerView.Adapter<TopupHistoryGroupAdapter.ViewHolder> {
        private final List<HistoryGroup> list;

        public TopupHistoryGroupAdapter(List<HistoryGroup> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_topup_history_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryGroup item = list.get(position);
            holder.tvGroupTitle.setText(item.title);
            holder.tvGroupAmount.setText("+" + CurrencyUtils.formatVND(item.amount));

            if (item.title.startsWith("Tháng")) {
                holder.tvGroupSubtitle.setText("Tổng nạp trong tháng");
            } else if (item.title.startsWith("Năm")) {
                holder.tvGroupSubtitle.setText("Tổng nạp trong năm");
            } else {
                holder.tvGroupSubtitle.setText("Tổng nạp trong ngày");
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvGroupTitle, tvGroupSubtitle, tvGroupAmount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
                tvGroupSubtitle = itemView.findViewById(R.id.tvGroupSubtitle);
                tvGroupAmount = itemView.findViewById(R.id.tvGroupAmount);
            }
        }
    }
}
