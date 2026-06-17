package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class Budget implements Serializable {
    private String budgetId;
    private String userId;
    private String categoryId; // Specific category ID or "all" for general budget
    private double limitAmount;
    private double currentSpent;
    private long startDate;
    private long endDate;

    public Budget() {
        // Required for Firebase deserialization
    }

    public Budget(String budgetId, String userId, String categoryId, double limitAmount, double currentSpent, long startDate, long endDate) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.currentSpent = currentSpent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public double getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(double currentSpent) {
        this.currentSpent = currentSpent;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }
}
