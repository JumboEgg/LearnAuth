package com.example.second_project.network

import com.example.second_project.data.model.dto.response.LogoutResponse
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/logout")
    fun logout(@Header("Refresh") refreshToken: String): Call<Void>
}