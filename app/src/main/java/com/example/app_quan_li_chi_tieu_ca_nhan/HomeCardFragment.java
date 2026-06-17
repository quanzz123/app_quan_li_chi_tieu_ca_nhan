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

/**
 * Fragment hiển thị màn hình chính dạng Thẻ (Card Home).
 * Tải và hiển thị danh sách rút gọn gồm 5 giao dịch gần nhất của người dùng.
 */
public class HomeCardFragment extends Fragment {

    private static final String TAG = "HomeCardFragment";
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout XML cho Fragment Card Home
        View view = inflater.inflate(R.layout.fragment_home_card, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        transactionList = new ArrayList<>();

        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        // Thiết lập Adapter hiển thị giao dịch kèm sự kiện click xem chi tiết
        adapter = new TransactionAdapter(transactionList, transaction -> {
            Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            startActivity(intent);
        });
        rvTransactions.setAdapter(adapter);

        // Tải 5 giao dịch gần đây nhất
        loadRecentTransactions();

        return view;
    }

    /**
     * Tải và lắng nghe 5 giao dịch gần đây nhất của người dùng hiện tại từ Firestore.
     */
    private void loadRecentTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Lỗi Firestore: " + error.getMessage(), error);
                        String msg = error.getMessage();
                        // Hướng dẫn nếu thiếu Index cho truy vấn orderBy + where
                        if (msg != null && msg.contains("index")) {
                            Toast.makeText(getContext(), "Cần tạo Index trên Firebase. Kiểm tra Logcat!", Toast.LENGTH_LONG).show();
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