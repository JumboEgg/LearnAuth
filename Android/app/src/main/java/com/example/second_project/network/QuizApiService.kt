package com.example.second_project.network

import com.example.second_project.data.model.dto.response.QuizResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface QuizApiService {
    @GET("/api/lecture/{lectureId}/quiz")
    fun getQuiz(@Path("lectureId") lectureId: Int): Call<QuizResponse>
} 