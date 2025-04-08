package com.example.second_project.network

import com.example.second_project.data.model.dto.response.CertificateDetailResponse
import com.example.second_project.data.model.dto.response.CertificateResponse
import com.example.second_project.data.model.dto.response.CertificateIssueResponse
import com.example.second_project.data.model.dto.response.CertificateVerifyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST

interface CertificateApiService {
    @GET("/api/certificate")
    fun getCertificates(@Query("userId") userId: Int): Call<CertificateResponse>

    @GET("/api/certificate/detail")
    fun getCertificateDetail(
        @Query("userId") userId: Int,
        @Query("lectureId") lectureId: Int
    ): Call<CertificateDetailResponse>

    @PATCH("api/certificate/lecture/{lectureId}/certification")
    fun issueCertificate(
        @Path("lectureId") lectureId: Int,
        @Body requestBody: CertificateIssueRequest
    ): Call<CertificateIssueResponse>
    
    @POST("/api/certificate/verify")
    fun verifyCertificate(
        @Body requestBody: CertificateVerifyRequest
    ): Call<CertificateVerifyResponse>
}

data class CertificateIssueRequest(
    val userId: Int,
    val cid: String
)

data class CertificateVerifyRequest(
    val tokenId: String
)