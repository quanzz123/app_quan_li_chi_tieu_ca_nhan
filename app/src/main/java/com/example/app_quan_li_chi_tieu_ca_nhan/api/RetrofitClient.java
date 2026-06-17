package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Lớp cấu hình Retrofit Client để thực hiện các cuộc gọi API mạng.
 * Quản lý các instance API cho ImgBB (upload ảnh) và LabelApi (phân loại nhãn từ backend).
 */
public class RetrofitClient {
    private static final String BASE_URL = "https://api.imgbb.com/";
    private static Retrofit retrofit = null;

    /**
     * Khởi tạo và trả về instance của ImgBBApi dùng để tải ảnh hóa đơn/sản phẩm lên hosting ImgBB.
     * Sử dụng mô hình Singleton cho Retrofit instance của ImgBB.
     */
    public static ImgBBApi getImgBBApi() {
        if (retrofit == null) {
            // Thiết lập logging để theo dõi request và response trong Logcat khi debug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(ImgBBApi.class);
    }

    /**
     * Khởi tạo và trả về instance của LabelApi để kết nối tới server backend phân loại nhãn.
     * Cần chỉnh sửa địa chỉ BASE_URL (IP máy tính hoặc ngrok) để phù hợp khi kết nối từ thiết bị thật.
     */
    public static LabelApi getLabelApi() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit labelRetrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.108:8000/") // Thay thành IP máy tính hoặc Ngrok nếu dùng thiết bị thật
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return labelRetrofit.create(LabelApi.class);
    }
}
