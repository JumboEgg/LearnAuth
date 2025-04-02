package com.example.second_project.network

import com.example.second_project.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 실제 API 서버의 Base URL로 변경하세요.
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val registerService: RegisterService = retrofit.create(RegisterService::class.java)
    val quizService: QuizApiService = retrofit.create(QuizApiService::class.java)
}