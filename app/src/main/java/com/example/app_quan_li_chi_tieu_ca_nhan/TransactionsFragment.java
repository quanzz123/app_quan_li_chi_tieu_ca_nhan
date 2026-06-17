package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.widget.Toast;
import com.example.app_quan_li_chi_tieu_ca_nhan.adapters.TransactionAdapter;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.TextView;

/**
 * Fragment hiển thị lịch sử toàn bộ các giao dịch.
 * Thực hiện truy vấn danh sách giao dịch từ Firestore, sắp xếp theo thời gian mới nhất,
 * và hỗ trợ bắt lỗi thiếu Index trên Firestore để hướng dẫn lập trình viên cấu hình.
 */
public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";
    private RecyclerView rvAllTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private TextView tvWelcome;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        transactionList = new ArrayList<>();

        tvWelcome = view.findViewById(R.id.tvWelcome);
        rvAllTransactions = view.findViewById(R.id.rvAllTransactions);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter cho danh sách giao dịch và thiết lập sự kiện khi click vào item
        adapter = new TransactionAdapter(transactionList, transaction -> {
            // Xem thông tin chi tiết giao dịch
            Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            startActivity(intent);
        });
        rvAllTransactions.setAdapter(adapter);

        // Lấy thông tin chào mừng và tải danh sách giao dịch
        fetchUserData();
        loadTransactions();

        return view;
    }

    /**
     * Lấy thông tin họ tên người dùng từ Firestore để cá nhân hóa lời chào.
     */
    private void fetchUserData() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty() && tvWelcome != null) {
                            tvWelcome.setText("Xin chào, " + fullName);
                        }
                    }
                });
    }

    /**
     * Tải và lắng nghe danh sách giao dịch thời gian thực từ Firestore của người dùng hiện tại.
     * Có sắp xếp theo dấu thời gian giảm dần (mới nhất lên đầu).
     */
    private void loadTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Log lỗi chi tiết của Firebase trong Logcat.
                        // Khi Firestore chưa được tạo Index cho tổ hợp Query (where + orderBy),
                        // Firebase SDK sẽ trả về lỗi kèm link trực tiếp để tạo Index tự động.
                        Log.e("FIREBASE_INDEX_ERROR", "--------------------------------------------------");
                        Log.e("FIREBASE_INDEX_ERROR", "Lỗi: " + error.getMessage());
                        Log.e("FIREBASE_INDEX_ERROR", "Full Error: " + error.toString());
                        Log.e("FIREBASE_INDEX_ERROR", "--------------------------------------------------");

                        String msg = error.getMessage();
                        if (msg != null && msg.contains("index")) {
                            Toast.makeText(getContext(), "Cần tạo Index trên Firebase. Kiểm tra Logcat để lấy link!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + msg, Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (value != null) {
                        transactionList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            transactionList.add(transaction);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
