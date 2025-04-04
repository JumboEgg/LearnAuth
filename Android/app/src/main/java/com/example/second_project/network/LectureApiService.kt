package com.example.second_project.network

import com.example.second_project.data.model.dto.request.SaveTimeRequest
import com.example.second_project.data.model.dto.response.LectureDetailResponse
import com.example.second_project.data.model.dto.response.LectureResponse
import com.example.second_project.data.model.dto.response.LectureSearchResponse
import com.example.second_project.data.model.dto.response.MostCompletedLecturesResponse
import com.example.second_project.data.model.dto.response.MostRecentLecturesResponse
import com.example.second_project.data.model.dto.response.OwnedLectureResponse
import com.example.second_project.data.model.dto.response.ParticipatedLectureResponse
import com.example.second_project.data.model.dto.response.RandomLecturesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.POST


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

    // 강의 검색
    @GET("/api/lecture/search")
    fun getSearchLectures(
        @Query("keyword") keyword: String,
        @Query("page") page: Int
    ): Call<LectureSearchResponse>

    // 내가 참여한 강의
    @GET("/api/lecture/participated")
    fun getParticipatedLectures(
        @Query("userId") userId:Int
    ) :Call<ParticipatedLectureResponse>

    // 내가 보유한 강의
    @GET("/api/lecture/owned")
    fun getOwnedLectures(
        @Query("userId") userId:Int
    ) : Call<OwnedLectureResponse>

    // 개별 강의 재생 시간 업데이트
    @POST("api/userlecture/{userLectureId}/time")
    fun saveWatchTime(
        @Path("userLectureId") userLectureId: Int,
        @Query("subLectureId") subLectureId: Int,
        @Body body: SaveTimeRequest
    ): Call<Void>

    // 마지막으로 시청한 개별강의 업데이트
    @PATCH("/api/userlecture/{userLectureId}/lastviewd")
    fun updateLastViewedLecture(
        @Path("userLectureId") userLectureId: Int,
        @Query("subLectureId") subLectureId: Int
    ): Call<Void>

}