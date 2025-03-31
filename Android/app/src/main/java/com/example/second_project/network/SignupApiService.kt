package com.example.second_project.network

import com.example.second_project.data.model.dto.request.SignupRequest
import com.example.second_project.data.model.dto.response.SignupResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SignupApiService {
    @POST("api/auth/signup")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>
}