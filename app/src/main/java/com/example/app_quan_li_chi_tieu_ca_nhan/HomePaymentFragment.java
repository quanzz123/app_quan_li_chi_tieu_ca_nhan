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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.adapters.ServiceAdapter;
import com.example.app_quan_li_chi_tieu_ca_nhan.adapters.TransactionAdapter;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.ServiceItem;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Balance;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * Fragment hiển thị màn hình chính (Trang chủ ví thanh toán).
 * Quản lý số dư hiển thị, các nút thao tác nhanh (Nạp tiền, Quét ảnh hóa đơn)
 * và danh sách các giao dịch gần đây.
 */
public class HomePaymentFragment extends Fragment {

    private RecyclerView rvServices, rvRecentTransactions;
    private ServiceAdapter adapter;
    private TransactionAdapter transactionAdapter;
    private TextView tvHomeBalance, tvHomeIncome, tvHomeExpense, tvWelcome;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout XML cho Fragment
        View view = inflater.inflate(R.layout.fragment_home_payment, container, false);

        // Khởi tạo các instance dịch vụ Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Ánh xạ các thành phần giao diện
        tvHomeBalance = view.findViewById(R.id.tvHomeBalance);
        tvHomeIncome = view.findViewById(R.id.tvHomeIncome);
        tvHomeExpense = view.findViewById(R.id.tvHomeExpense);
        tvWelcome = view.findViewById(R.id.tvWelcome);

        // 1. Khởi tạo danh sách các dịch vụ nhanh (Drinks, Food, Shopping) dạng Grid 3 cột
        rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ServiceAdapter(getDummyServices(), service -> {
            // Khi nhấn vào dịch vụ, chuyển sang màn hình thêm giao dịch thủ công
            Intent intent = new Intent(getActivity(), AddActivity.class);
            intent.putExtra("categoryName", service.getName());
            startActivity(intent);
        });
        rvServices.setAdapter(adapter);

        // 2. Khởi tạo danh sách lịch sử giao dịch gần đây
        rvRecentTransactions = view.findViewById(R.id.rvHomeRecentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Cấu hình các nút chức năng nhanh
        setupQuickActions(view);
        
        // Đồng bộ dữ liệu
        fetchUserData();
        listenForBalance();
        listenForRecentTransactions();

        return view;
    }

    /**
     * Lấy tên người dùng hiện tại từ tài khoản Firestore để hiển thị lời chào.
     */
    private void fetchUserData() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            tvWelcome.setText("Xin chào, " + fullName);
                        }
                    }
                });
    }

    /**
     * Khởi tạo các nút thao tác nhanh trên Trang chủ (Nạp tiền, Chuyển tiền, Quét ảnh hóa đơn).
     */
    private void setupQuickActions(View view) {
        // Nút nạp tiền ví điện tử
        View actionTopup = view.findViewById(R.id.actionTopup);
        ((TextView) actionTopup.findViewById(R.id.tvActionName)).setText("Nạp tiền");
        ((ImageView) actionTopup.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_input_add);
        actionTopup.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TopupActivity.class));
        });

        // Nút chuyển tiền (Chưa phát triển hoàn thiện)
        View actionTransfer = view.findViewById(R.id.actionTransfer);
        ((TextView) actionTransfer.findViewById(R.id.tvActionName)).setText("Chuyển tiền");
        ((ImageView) actionTransfer.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_menu_send);

        // Nút quét ảnh hóa đơn bằng máy ảnh AI
        View actionScan = view.findViewById(R.id.actionScan);
        ((TextView) actionScan.findViewById(R.id.tvActionName)).setText("Quét ảnh");
        ((ImageView) actionScan.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_menu_camera);
        actionScan.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CaptureTransactionActivity.class));
        });
    }

    /**
     * Lắng nghe biến động số dư tài khoản thời gian thực từ Firestore.
     */
    private void listenForBalance() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("balances").document(userId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        Balance balance = value.toObject(Balance.class);
                        if (balance != null) {
                            // Cập nhật số dư hiện tại lên giao diện
                            tvHomeBalance.setText(CurrencyUtils.formatVND(balance.getCurrentBalance()));
                        }
                    } else {
                        tvHomeBalance.setText(CurrencyUtils.formatVND(0.0));
                    }
                });
    }

    /**
     * Lắng nghe và hiển thị tối đa 5 giao dịch gần nhất của người dùng từ Firestore.
     */
    private void listenForRecentTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<Transaction> list = value.toObjects(Transaction.class);
                        transactionAdapter = new TransactionAdapter(list, transaction -> {
                            // Khi nhấn vào giao dịch, chuyển đến xem chi tiết
                            Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                            intent.putExtra("transaction", transaction);
                            startActivity(intent);
                        });
                        rvRecentTransactions.setAdapter(transactionAdapter);
                        
                        // Cập nhật thống kê nhanh (Thu nhập / Chi tiêu) dựa trên 5 giao dịch này
                        updateStatistics(list);
                    }
                });
    }

    /**
     * Tính toán tổng thu và tổng chi trong các giao dịch gần đây để hiển thị lên thẻ ví.
     */
    private void updateStatistics(List<Transaction> transactions) {
        double income = 0;
        double expense = 0;
        for (Transaction t : transactions) {
            if (t.isExpense()) {
                expense += t.getAmount();
            } else {
                income += t.getAmount();
            }
        }
        tvHomeIncome.setText("+ " + CurrencyUtils.formatVND(income));
        tvHomeExpense.setText("- " + CurrencyUtils.formatVND(expense));
    }

    /**
     * Khởi tạo danh sách dữ liệu mẫu cho các dịch vụ nhanh.
     */
    private List<ServiceItem> getDummyServices() {
        List<ServiceItem> list = new ArrayList<>();
        list.add(new ServiceItem("Drinks", R.drawable.drink_service_icon));
        list.add(new ServiceItem("Food", R.drawable.food_service_icon));
        list.add(new ServiceItem("Shopping", R.drawable.shopping_service_icon));

        return list;
    }
}
