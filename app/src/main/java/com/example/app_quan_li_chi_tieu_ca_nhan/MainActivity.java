package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.rvTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TransactionAdapter(Arrays.asList(
                new Transaction("$", "Lương tháng 5", "01/05/2026", "+18.000.000 đ", true),
                new Transaction("F", "Ăn trưa", "01/05/2026", "-120.000 đ", false),
                new Transaction("T", "Grab đi làm", "30/04/2026", "-75.000 đ", false),
                new Transaction("S", "Mua áo sơ mi", "29/04/2026", "-450.000 đ", false),
                new Transaction("C", "Cà phê với bạn", "28/04/2026", "-65.000 đ", false),
                new Transaction("B", "Thưởng dự án", "27/04/2026", "+2.500.000 đ", true)
        )));

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddActivity.class))
        );
    }

    static class Transaction {
        final String icon;
        final String title;
        final String date;
        final String amount;
        final boolean income;

        Transaction(String icon, String title, String date, String amount, boolean income) {
            this.icon = icon;
            this.title = title;
            this.date = date;
            this.amount = amount;
            this.income = income;
        }
    }

    static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private final List<Transaction> transactions;

        TransactionAdapter(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction item = transactions.get(position);
            holder.icon.setText(item.icon);
            holder.title.setText(item.title);
            holder.date.setText(item.date);
            holder.amount.setText(item.amount);
            holder.amount.setTextColor(Color.parseColor(item.income ? "#059669" : "#DC2626"));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView icon;
            final TextView title;
            final TextView date;
            final TextView amount;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.tvCategoryIcon);
                title = itemView.findViewById(R.id.tvTitle);
                date = itemView.findViewById(R.id.tvDate);
                amount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
