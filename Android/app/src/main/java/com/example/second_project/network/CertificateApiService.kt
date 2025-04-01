package com.example.second_project.network

import com.example.second_project.data.model.dto.response.CertificateDetailResponse
import com.example.second_project.data.model.dto.response.CertificateResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CertificateApiService {
    @GET("/api/certificate")
    fun getCertificates(@Query("userId") userId: Int): Call<CertificateResponse>

    @GET("/api/certificate/detail")
    fun getCertificateDetail(
        @Query("userId") userId: Int,
        @Query("lectureId") lectureId: Int
    ): Call<CertificateDetailResponse>
}