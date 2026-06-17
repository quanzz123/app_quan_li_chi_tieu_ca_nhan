package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Interface Retrofit định nghĩa yêu cầu tải ảnh lên dịch vụ lưu trữ ảnh miễn phí ImgBB.
 */
public interface ImgBBApi {
    
    /**
     * Tải hình ảnh lên ImgBB dưới dạng dữ liệu Multipart.
     *
     * @param apiKey Khóa API được cung cấp bởi ImgBB để xác thực quyền upload
     * @param image Đối tượng MultipartBody.Part chứa dữ liệu nhị phân của hình ảnh cần tải lên
     * @return Call chứa đối tượng phản hồi ImgBBResponse sau khi upload thành công (có chứa URL ảnh)
     */
    @Multipart
    @POST("1/upload")
    Call<ImgBBResponse> uploadImage(
            @Query("key") String apiKey,
            @Part MultipartBody.Part image
    );
}
