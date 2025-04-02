package com.example.second_project.network

import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.model.dto.response.LectureResponse
import com.example.second_project.data.model.dto.response.MostCompletedLecturesResponse
import com.example.second_project.data.model.dto.response.MostRecentLecturesResponse
import com.example.second_project.data.model.dto.response.RandomLecturesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface LectureApiService {
    // 카테고리별 강의 불러오기
    @GET("api/lecture")
    fun getLectures(
        @Query("categoryId") categoryId: Int,
        @Query("page") page: Int
    ): Call<LectureResponse>

    // 강의 디테일
    @GET("api/lecture/{lectureId}")
    fun getLectureDetail(
        @Path("lectureId") lectureId: Int,
        @Query("userId") userId: Int
    ): Call<LectureDetailResponse>

    // 가장 많이 이수한 강의 최대 3개
    @GET("/api/lecture/mostCompleted")
    fun getMostCompletedLectures(): Call<MostCompletedLecturesResponse>

    // 가장 최근 등록 강의 10개
    @GET("/api/lecture/mostRecent")
    fun getMostRecentLectures(): Call<MostRecentLecturesResponse>

    // 랜덤 강의 10개 (추천용)
    @GET("/api/lecture/random")
    fun getRandomLectures(): Call<RandomLecturesResponse>
}