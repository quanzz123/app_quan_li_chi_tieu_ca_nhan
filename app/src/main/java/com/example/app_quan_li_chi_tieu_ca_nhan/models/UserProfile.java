package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String userId;
    private String avatarUrl;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String occupation;
    private double monthlyIncomeGoal;
    private double savingGoal;
    private String themePreference; // "light" or "dark"
    private String languagePreference; // "vi" or "en"
    private boolean notificationsEnabled;
    private String bio;

    public UserProfile() {
        // Required for Firebase deserialization
        this.themePreference = "light";
        this.languagePreference = "vi";
        this.notificationsEnabled = true;
    }

    public UserProfile(String userId, String avatarUrl, String phoneNumber, String dateOfBirth, 
                       String gender, String address, String occupation, double monthlyIncomeGoal, 
                       double savingGoal, String themePreference, String languagePreference, 
                       boolean notificationsEnabled, String bio) {
        this.userId = userId;
        this.avatarUrl = avatarUrl;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.occupation = occupation;
        this.monthlyIncomeGoal = monthlyIncomeGoal;
        this.savingGoal = savingGoal;
        this.themePreference = themePreference;
        this.languagePreference = languagePreference;
        this.notificationsEnabled = notificationsEnabled;
        this.bio = bio;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public double getMonthlyIncomeGoal() {
        return monthlyIncomeGoal;
    }

    public void setMonthlyIncomeGoal(double monthlyIncomeGoal) {
        this.monthlyIncomeGoal = monthlyIncomeGoal;
    }

    public double getSavingGoal() {
        return savingGoal;
    }

    public void setSavingGoal(double savingGoal) {
        this.savingGoal = savingGoal;
    }

    public String getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(String themePreference) {
        this.themePreference = themePreference;
    }

    public String getLanguagePreference() {
        return languagePreference;
    }

    public void setLanguagePreference(String languagePreference) {
        this.languagePreference = languagePreference;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
