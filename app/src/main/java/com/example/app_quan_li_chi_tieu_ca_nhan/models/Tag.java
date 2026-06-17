package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class Tag implements Serializable {
    private String tagId;
    private String userId;
    private String name;
    private String colorHex;

    public Tag() {
        // Required for Firebase deserialization
    }

    public Tag(String tagId, String userId, String name, String colorHex) {
        this.tagId = tagId;
        this.userId = userId;
        this.name = name;
        this.colorHex = colorHex;
    }

    // Getters and Setters
    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
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

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
