package com.example.app_quan_li_chi_tieu_ca_nhan.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.imgbb.com/";
    private static Retrofit retrofit = null;

    public static ImgBBApi getImgBBApi() {
        if (retrofit == null) {
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
