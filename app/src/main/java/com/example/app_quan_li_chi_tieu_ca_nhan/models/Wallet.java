package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class Wallet implements Serializable {
    private String walletId;
    private String userId;
    private String name;
    private double balance;
    private String colorCode; // Hex color for UI customization
    private String walletType; // "Cash", "Bank", "Credit Card", "E-Wallet"
    private long lastUpdated;

    public Wallet() {
        // Required for Firebase deserialization
    }

    public Wallet(String walletId, String userId, String name, double balance, String colorCode, String walletType, long lastUpdated) {
        this.walletId = walletId;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.colorCode = colorCode;
        this.walletType = walletType;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
