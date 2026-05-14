package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import com.google.gson.annotations.SerializedName;

public class ImgBBResponse {
    @SerializedName("data")
    private Data data;

    @SerializedName("success")
    private boolean success;

    @SerializedName("status")
    private int status;

    public Data getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public static class Data {
        @SerializedName("url")
        private String url;

        @SerializedName("display_url")
        private String displayUrl;

        public String getUrl() {
            return url;
        }

        public String getDisplayUrl() {
            return displayUrl;
        }
    }
}
