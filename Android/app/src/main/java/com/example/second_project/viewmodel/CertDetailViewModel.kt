package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.model.dto.response.CertificateDetailResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CertDetailViewModel : ViewModel() {
    private val _certificateDetail = MutableLiveData<CertificateDetailResponse?>()
    val certificateDetail: LiveData<CertificateDetailResponse?> = _certificateDetail

    fun fetchCertificateDetail(userId: String, lectureId: String) {
        val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
        certificateApiService.getCertificateDetail(userId, lectureId)
            .enqueue(object : Callback<CertificateDetailResponse> {
                override fun onResponse(
                    call: Call<CertificateDetailResponse>,
                    response: Response<CertificateDetailResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _certificateDetail.value = response.body()
                    } else {
                        _certificateDetail.value = null
                    }
                }

                override fun onFailure(call: Call<CertificateDetailResponse>, t: Throwable) {
                    _certificateDetail.value = null
                }
            })
    }
}
