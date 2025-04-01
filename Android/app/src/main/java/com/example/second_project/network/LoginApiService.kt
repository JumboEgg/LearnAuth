package com.example.second_project.network

import com.example.second_project.data.model.dto.request.LogInRequest
import com.example.second_project.data.model.dto.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    @POST("/api/auth/login")
    fun login(@Body request : LogInRequest): Call<LoginResponse>
}