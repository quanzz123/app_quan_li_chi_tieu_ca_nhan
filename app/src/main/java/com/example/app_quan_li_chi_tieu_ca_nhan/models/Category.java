package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String categoryId;
    private String userId; // Null for default/system-wide categories, set for user-defined ones
    private String name;
    private String iconName; // Stores drawable resource name (e.g. "food_service_icon")
    private String colorHex; // Hex color for UI representation
    private boolean isExpense;
    private boolean isCustom;

    public Category() {
        // Required for Firebase deserialization
    }

    public Category(String categoryId, String userId, String name, String iconName, String colorHex, boolean isExpense, boolean isCustom) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.iconName = iconName;
        this.colorHex = colorHex;
        this.isExpense = isExpense;
        this.isCustom = isCustom;
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
