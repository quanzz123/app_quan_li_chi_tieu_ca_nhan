package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {
    private String title;
    private String categoryName;
    private String date;
    private double amount;
    private int iconRes;
    private boolean isExpense;
    private String imageUrl;
    
    // Extended fields for future scope
    private String userId;
    private String walletId;
    private String categoryId;
    private String notes;
    private List<String> tagIds;
    private long timestamp;

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

    public Transaction(String title, String categoryName, String date, double amount, int iconRes, boolean isExpense, 
                       String imageUrl, String userId, String walletId, String categoryId, String notes, 
                       List<String> tagIds, long timestamp) {
        this.title = title;
        this.categoryName = categoryName;
        this.date = date;
        this.amount = amount;
        this.iconRes = iconRes;
        this.isExpense = isExpense;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.notes = notes;
        this.tagIds = tagIds;
        this.timestamp = timestamp;
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

    // Extended Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<String> tagIds) {
        this.tagIds = tagIds;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

