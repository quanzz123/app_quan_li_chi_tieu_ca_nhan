package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import com.google.gson.annotations.SerializedName;

public class LabelResponse {
    @SerializedName("prediction")
    private String prediction;

    @SerializedName("confidence")
    private double confidence;

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public double getConfidence() {
        return confidence;
    }
}
