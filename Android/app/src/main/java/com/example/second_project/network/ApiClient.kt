package com.example.second_project.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 실제 API 서버의 Base URL로 변경하세요.
    private const val BASE_URL = "https://j12d210.p.ssafy.io"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}