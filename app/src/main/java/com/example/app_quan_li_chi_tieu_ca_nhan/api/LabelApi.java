package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Interface Retrofit định nghĩa kết nối tới Server API máy học (FastAPI/Flask)
 * để nhận diện nhãn sản phẩm/hóa đơn từ hình ảnh được gửi lên.
 */
public interface LabelApi {
    
    /**
     * Gửi file ảnh lên server AI để nhận diện nhãn phân loại giao dịch.
     *
     * @param file Đối tượng chứa ảnh cần phân tích dưới dạng MultipartBody.Part
     * @return Call chứa kết quả phân loại LabelResponse (nhãn nháp và độ tin cậy)
     */
    @Multipart
    @POST("/predict")
    Call<LabelResponse> getLabelFromFile(@Part MultipartBody.Part file);
}
