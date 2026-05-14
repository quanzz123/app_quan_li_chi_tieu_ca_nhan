package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.bumptech.glide.Glide;
import com.example.app_quan_li_chi_tieu_ca_nhan.models.Transaction;
import com.example.app_quan_li_chi_tieu_ca_nhan.utils.CurrencyUtils;

public class TransactionDetailActivity extends AppCompatActivity {

    private ImageView ivDetailIcon, ivTransactionDetailImage;
    private TextView tvDetailAmount, tvDetailTitle, tvDetailCategory, tvDetailDate;
    private View cardTransactionImage;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        initViews();

        Transaction transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        if (transaction != null) {
            displayTransactionDetails(transaction);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        ivDetailIcon = findViewById(R.id.ivDetailIcon);
        ivTransactionDetailImage = findViewById(R.id.ivTransactionDetailImage);
        tvDetailAmount = findViewById(R.id.tvDetailAmount);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        cardTransactionImage = findViewById(R.id.cardTransactionImage);
        toolbar = findViewById(R.id.toolbar);
    }

    private void displayTransactionDetails(Transaction transaction) {
        tvDetailTitle.setText(transaction.getTitle());
        tvDetailCategory.setText(transaction.getCategoryName());
        tvDetailDate.setText(transaction.getDate());

        tvDetailAmount.setText(CurrencyUtils.formatTransactionAmount(transaction.getAmount(), transaction.isExpense()));
        tvDetailAmount.setTextColor(transaction.isExpense() ? Color.parseColor("#EF4444") : Color.parseColor("#10B981"));

        // Set transaction image if available
        if (transaction.getImageUrl() != null && !transaction.getImageUrl().isEmpty()) {
            cardTransactionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(transaction.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivTransactionDetailImage);
        } else {
            cardTransactionImage.setVisibility(View.GONE);
        }

        // Set icon based on category
        if (transaction.getCategoryName() != null) {
            switch (transaction.getCategoryName().toLowerCase()) {
                case "food":
                    ivDetailIcon.setImageResource(R.drawable.food_service_icon);
                    break;
                case "drinks":
                    ivDetailIcon.setImageResource(R.drawable.drink_service_icon);
                    break;
                case "shopping":
                    ivDetailIcon.setImageResource(R.drawable.shopping_service_icon);
                    break;
                default:
                    ivDetailIcon.setImageResource(android.R.drawable.ic_menu_gallery);
                    break;
            }
        }
    }
}
