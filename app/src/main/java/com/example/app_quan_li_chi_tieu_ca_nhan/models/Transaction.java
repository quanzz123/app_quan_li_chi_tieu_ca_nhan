package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Transaction implements Serializable {
    private String title;
    private String categoryName;
    private String date;
    private double amount;
    private int iconRes;
    private boolean isExpense;
    private String imageUrl;

    public Transaction() {
    }

    public Transaction(String title, String categoryName, String date, double amount, int iconRes, boolean isExpense) {
        this.title = title;
        this.categoryName = categoryName;
        this.date = date;
        this.amount = amount;
        this.iconRes = iconRes;
        this.isExpense = isExpense;
    }

    public Transaction(String title, String categoryName, String date, double amount, int iconRes, boolean isExpense, String imageUrl) {
        this.title = title;
        this.categoryName = categoryName;
        this.date = date;
        this.amount = amount;
        this.iconRes = iconRes;
        this.isExpense = isExpense;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    @PropertyName("isExpense")
    public boolean isExpense() {
        return isExpense;
    }

    @PropertyName("isExpense")
    public void setIsExpense(boolean expense) {
        isExpense = expense;
    }
}
