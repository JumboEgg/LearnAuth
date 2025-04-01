package com.example.second_project.network

import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.model.dto.response.LectureResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface LectureApiService {
    @GET("api/lecture")
    fun getLectures(
        @Query("categoryId") categoryId: Int,
        @Query("page") page: Int
    ): Call<LectureResponse>

    @GET("api/lecture/{lectureId}")
    fun getLectureDetail(
        @Path("lectureId") lectureId: Int,
        @Query("userId") userId: Int
    ): Call<LectureDetailResponse>
}