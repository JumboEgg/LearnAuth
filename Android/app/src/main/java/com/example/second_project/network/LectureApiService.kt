package com.example.second_project.network

import com.example.second_project.data.model.dto.response.LectureResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface LectureApiService {
    @GET("api/lecture")
    fun getLectures(
        @Query("category") category: String,
        @Query("page") page: Int
    ): Call<LectureResponse>
}