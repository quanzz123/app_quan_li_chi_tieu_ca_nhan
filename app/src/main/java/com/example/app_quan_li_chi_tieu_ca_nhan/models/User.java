package com.example.app_quan_li_chi_tieu_ca_nhan.models;

public class User {
    private String fullName;
    private String email;
    private String userId;

    public User() {
    }

    public User(String fullName, String email, String userId) {
        this.fullName = fullName;
        this.email = email;
        this.userId = userId;
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
}
