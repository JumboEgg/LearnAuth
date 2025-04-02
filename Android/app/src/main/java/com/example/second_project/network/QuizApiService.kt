package com.example.second_project.network

import com.example.second_project.data.model.dto.response.QuizResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body

interface QuizApiService {
    @GET("/api/lecture/{lectureId}/quiz")
    fun getQuiz(@Path("lectureId") lectureId: Int): Call<QuizResponse>

    @POST("/api/lecture/{lectureId}/quiz")
    fun completeQuiz(
        @Path("lectureId") lectureId: Int,
        @Body request: QuizCompleteRequest
    ): Call<QuizResponse>
}

data class QuizCompleteRequest(
    val completeQuiz: Boolean,
    val userId: Int
) 