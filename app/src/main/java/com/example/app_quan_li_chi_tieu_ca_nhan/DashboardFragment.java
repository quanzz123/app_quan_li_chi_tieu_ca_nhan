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
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tvBalanceAmount = view.findViewById(R.id.tvBalanceAmount);

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

        listenForBalance();

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

    private List<ServiceItem> getDummyServices() {
        List<ServiceItem> list = new ArrayList<>();
        list.add(new ServiceItem("Drinks", R.drawable.drink_service_icon));
        list.add(new ServiceItem("Food", R.drawable.food_service_icon));
        list.add(new ServiceItem("Shopping", R.drawable.shopping_service_icon));

        return list;
    }
}
