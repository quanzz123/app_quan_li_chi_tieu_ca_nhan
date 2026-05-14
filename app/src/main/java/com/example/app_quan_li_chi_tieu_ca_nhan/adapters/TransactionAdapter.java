package com.example.app_quan_li_chi_tieu_ca_nhan.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.R;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public TransactionAdapter(List<Transaction> transactions, OnItemClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.tvTitle.setText(transaction.getTitle());
        holder.tvDate.setText(transaction.getDate());
        
        holder.tvAmount.setText(CurrencyUtils.formatTransactionAmount(transaction.getAmount(), transaction.isExpense()));
        holder.tvAmount.setTextColor(transaction.isExpense() ? Color.parseColor("#EF4444") : Color.parseColor("#10B981"));
        
        // Show image indicator
        if (transaction.getImageUrl() != null && !transaction.getImageUrl().isEmpty()) {
            holder.ivHasImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivHasImage.setVisibility(View.GONE);
        }

        // Set icon based on categoryName if iconRes is not set
        if (transaction.getIconRes() != 0) {
            holder.ivCategoryIcon.setImageResource(transaction.getIconRes());
        } else if (transaction.getCategoryName() != null) {
            switch (transaction.getCategoryName().toLowerCase()) {
                case "food":
                    holder.ivCategoryIcon.setImageResource(R.drawable.food_service_icon);
                    break;
                case "drinks":
                    holder.ivCategoryIcon.setImageResource(R.drawable.drink_service_icon);
                    break;
                case "shopping":
                    holder.ivCategoryIcon.setImageResource(R.drawable.shopping_service_icon);
                    break;
                default:
                    holder.ivCategoryIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                    break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon, ivHasImage;
        TextView tvTitle, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivHasImage = itemView.findViewById(R.id.ivHasImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
