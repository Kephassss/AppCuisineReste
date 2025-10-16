package com.repasdelaflemme.app.data.remote;

import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MealDbClient {
    private final MealDbApi api;

    public MealDbClient() {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient ok = new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com")
                .client(ok)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(MealDbApi.class);
    }

    public interface OnResult { void onSuccess(@Nullable MealDbResponse res); void onError(Throwable t); }

    @MainThread
    public void search(String q, OnResult cb) {
        api.search(q).enqueue(new Callback<MealDbResponse>() {
            @Override public void onResponse(Call<MealDbResponse> call, Response<MealDbResponse> response) {
                if (response.isSuccessful()) cb.onSuccess(response.body()); else cb.onError(new RuntimeException("HTTP "+response.code()));
            }
            @Override public void onFailure(Call<MealDbResponse> call, Throwable t) { cb.onError(t); }
        });
    }
}

