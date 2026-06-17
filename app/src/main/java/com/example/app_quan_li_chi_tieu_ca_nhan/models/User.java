package com.example.app_quan_li_chi_tieu_ca_nhan.models;

public class User {
    private String fullName;
    private String email;
    private String userId;
    private String defaultCurrency = "VND"; // Default currency for backward compatibility

    public User() {
    }

    public User(String fullName, String email, String userId) {
        this.fullName = fullName;
        this.email = email;
        this.userId = userId;
        this.defaultCurrency = "VND";
    }

    public User(String fullName, String email, String userId, String defaultCurrency) {
        this.fullName = fullName;
        this.email = email;
        this.userId = userId;
        this.defaultCurrency = defaultCurrency;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
}

