package com.example.second_project.network

import com.example.second_project.data.model.dto.request.DepositRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH

interface PaymentApiService {
    @PATCH("/api/payment/deposit")
    fun deposit(@Body request : DepositRequest) : Call<Void>
}