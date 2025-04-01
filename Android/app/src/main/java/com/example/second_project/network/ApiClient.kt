package com.example.second_project.network

import com.example.second_project.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//
object ApiClient {
    // 실제 API 서버의 Base URL로 변경하세요.

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}