package com.jon.common.versioncheck;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

object RetrofitClient {
    fun get(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private const val BASE_URL = "https://api.github.com";
}
