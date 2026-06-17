package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LabelApi {
    @Multipart
    @POST("/predict")
    Call<LabelResponse> getLabelFromFile(@Part MultipartBody.Part file);
}
