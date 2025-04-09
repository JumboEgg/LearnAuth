package com.example.second_project.network

import com.example.second_project.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Gson 객체 추가
    val gson: Gson = GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Double::class.java, InfinityTypeAdapter())
        .registerTypeAdapter(String::class.java, StringInfinityTypeAdapter())
        .create()
    
    // 실제 API 서버의 Base URL로 변경하세요.
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃
        .readTimeout(60, TimeUnit.SECONDS)    // 응답 대기 시간
        .writeTimeout(60, TimeUnit.SECONDS)   // 요청 송신 제한
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val registerService: RegisterService = retrofit.create(RegisterService::class.java)
    val quizService: QuizApiService = retrofit.create(QuizApiService::class.java)
    val certificateApiService: CertificateApiService = retrofit.create(CertificateApiService::class.java)
}

// Infinity 값을 처리하기 위한 TypeAdapter
class InfinityTypeAdapter : com.google.gson.TypeAdapter<Double>() {
    override fun write(out: com.google.gson.stream.JsonWriter, value: Double?) {
        if (value == null || value.isInfinite() || value.isNaN()) {
            out.value(0.0)
        } else {
            out.value(value)
        }
    }

    override fun read(input: com.google.gson.stream.JsonReader): Double {
        return try {
            input.nextDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}

// String 타입의 Infinity 값을 처리하기 위한 TypeAdapter
class StringInfinityTypeAdapter : com.google.gson.TypeAdapter<String>() {
    override fun write(out: com.google.gson.stream.JsonWriter, value: String?) {
        if (value == null || value.equals("Infinity", ignoreCase = true) || value.equals("NaN", ignoreCase = true)) {
            out.value("0.0")
        } else {
            out.value(value)
        }
    }

    override fun read(input: com.google.gson.stream.JsonReader): String {
        return try {
            val value = input.nextString()
            if (value.equals("Infinity", ignoreCase = true) || value.equals("NaN", ignoreCase = true)) {
                "0.0"
            } else {
                value
            }
        } catch (e: Exception) {
            "0.0"
        }
    }
}