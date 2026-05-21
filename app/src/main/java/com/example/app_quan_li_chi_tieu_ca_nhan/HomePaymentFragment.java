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

import android.widget.ImageView;
import android.widget.TextView;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Balance;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomePaymentFragment extends Fragment {

    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private TextView tvHomeBalance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_payment, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tvHomeBalance = view.findViewById(R.id.tvHomeBalance);

        rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new ServiceAdapter(getDummyServices(), service -> {
            Intent intent = new Intent(getActivity(), AddActivity.class);
            intent.putExtra("categoryName", service.getName());
            startActivity(intent);
        });
        rvServices.setAdapter(adapter);

        setupQuickActions(view);
        listenForBalance();

        return view;
    }

    private void setupQuickActions(View view) {
        View actionTopup = view.findViewById(R.id.actionTopup);
        ((TextView) actionTopup.findViewById(R.id.tvActionName)).setText("Topup");
        ((ImageView) actionTopup.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_input_add);
        actionTopup.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TopupActivity.class));
        });

        View actionTransfer = view.findViewById(R.id.actionTransfer);
        ((TextView) actionTransfer.findViewById(R.id.tvActionName)).setText("Transfer");
        ((ImageView) actionTransfer.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_menu_send);

        View actionScan = view.findViewById(R.id.actionScan);
        ((TextView) actionScan.findViewById(R.id.tvActionName)).setText("Scan");
        ((ImageView) actionScan.findViewById(R.id.ivActionIcon)).setImageResource(android.R.drawable.ic_menu_camera);
        actionScan.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CaptureTransactionActivity.class));
        });
    }

    private void listenForBalance() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("balances").document(userId)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        Balance balance = value.toObject(Balance.class);
                        if (balance != null) {
                            tvHomeBalance.setText(CurrencyUtils.formatVND(balance.getCurrentBalance()));
                        }
                    } else {
                        tvHomeBalance.setText(CurrencyUtils.formatVND(0.0));
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
