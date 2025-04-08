package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.CertificateVerifyResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateApiService
import com.example.second_project.network.CertificateVerifyRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CertVerifyViewModel : ViewModel() {
    private val _certificateVerifyResult = MutableLiveData<CertificateVerifyResponse?>()
    val certificateVerifyResult: LiveData<CertificateVerifyResponse?> = _certificateVerifyResult
    
    fun verifyCertificate(tokenId: String) {
        val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
        certificateApiService.verifyCertificate(
            requestBody = CertificateVerifyRequest(tokenId = tokenId)
        ).enqueue(object : Callback<CertificateVerifyResponse> {
            override fun onResponse(
                call: Call<CertificateVerifyResponse>,
                response: Response<CertificateVerifyResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    _certificateVerifyResult.value = response.body()
                } else {
                    _certificateVerifyResult.value = null
                }
            }

            override fun onFailure(call: Call<CertificateVerifyResponse>, t: Throwable) {
                _certificateVerifyResult.value = null
            }
        })
    }
} 