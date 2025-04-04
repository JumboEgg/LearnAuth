package com.example.second_project.network

import com.example.second_project.blockchain.SignedRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MetaTxApiService {

    @POST("/meta/execute")
    fun sendMetaTransaction(
        @Body signedRequest: SignedRequest
    ): Call<Void> // 또는 ResponseBody 등으로 받을 수 있음
}
