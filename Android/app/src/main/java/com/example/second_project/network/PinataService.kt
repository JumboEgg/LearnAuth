package com.example.second_project.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PinataService {
    @Multipart
    @POST("pinning/pinFileToIPFS")
    suspend fun pinFileToIPFS(
        @Header("Authorization") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<PinataResponse>
}

data class PinataResponse(
    val IpfsHash: String,
    val PinSize: Int,
    val Timestamp: String
) 