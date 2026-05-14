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

public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";
    private RecyclerView rvAllTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        transactionList = new ArrayList<>();

        rvAllTransactions = view.findViewById(R.id.rvAllTransactions);
        rvAllTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransactionAdapter(transactionList, transaction -> {
            Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            startActivity(intent);
        });
        rvAllTransactions.setAdapter(adapter);

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Log chi tiết để lấy link tạo index
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
