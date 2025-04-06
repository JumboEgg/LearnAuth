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
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃
        .readTimeout(60, TimeUnit.SECONDS)    // 응답 대기 시간
        .writeTimeout(60, TimeUnit.SECONDS)   // 요청 송신 제한
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient) // 👈 이거 빠져있었음!
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val registerService: RegisterService = retrofit.create(RegisterService::class.java)
    val quizService: QuizApiService = retrofit.create(QuizApiService::class.java)
}