package com.example.app_quan_li_chi_tieu_ca_nhan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_quan_li_chi_tieu_ca_nhan.R;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.ServiceItem;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private final List<ServiceItem> services;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ServiceItem service);
    }

    public ServiceAdapter(List<ServiceItem> services, OnItemClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceItem service = services.get(position);
        holder.tvServiceName.setText(service.getName());
        if (service.getIconRes() != 0) {
            holder.ivServiceIcon.setImageResource(service.getIconRes());
            // Xóa tint để hiển thị màu gốc của drawable nếu cần
            holder.ivServiceIcon.setImageTintList(null);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceIcon;
        TextView tvServiceName;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
        }
    }
}
