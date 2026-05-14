package com.example.app_quan_li_chi_tieu_ca_nhan.models;

public class ServiceItem {
    private String name;
    private int iconRes;

    public ServiceItem() {
    }

    public ServiceItem(String name, int iconRes) {
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
}
