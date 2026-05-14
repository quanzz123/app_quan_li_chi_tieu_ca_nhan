package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class Balance implements Serializable {
    private String userId;
    private double currentBalance;
    private long lastUpdated;
    private String currency;

    public Balance() {
    }

    public Balance(String userId, double currentBalance, long lastUpdated, String currency) {
        this.userId = userId;
        this.currentBalance = currentBalance;
        this.lastUpdated = lastUpdated;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
