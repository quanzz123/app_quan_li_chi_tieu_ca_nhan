package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class RecurringTransaction implements Serializable {
    private String recurringId;
    private String userId;
    private String walletId;
    private String categoryId;
    private String title;
    private double amount;
    private String frequency; // "daily", "weekly", "monthly", "yearly"
    private long nextExecutionDate;
    private boolean isActive;

    public RecurringTransaction() {
        // Required for Firebase deserialization
    }

    public RecurringTransaction(String recurringId, String userId, String walletId, String categoryId, String title, double amount, String frequency, long nextExecutionDate, boolean isActive) {
        this.recurringId = recurringId;
        this.userId = userId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.frequency = frequency;
        this.nextExecutionDate = nextExecutionDate;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getRecurringId() {
        return recurringId;
    }

    public void setRecurringId(String recurringId) {
        this.recurringId = recurringId;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public long getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(long nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
