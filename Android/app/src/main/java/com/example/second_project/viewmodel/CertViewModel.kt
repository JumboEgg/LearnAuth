package com.example.second_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.UserSession
import com.example.second_project.data.model.dto.response.CertificateData
import com.example.second_project.data.model.dto.response.CertificateResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.CertificateApiService
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class CertViewModel : ViewModel() {
    private val _certificateList = MutableLiveData<List<CertificateData>>()
    val certificateList: LiveData<List<CertificateData>> = _certificateList

    init {
        fetchCertificates(userId = UserSession.userId)
    }

    fun fetchCertificates(userId: Int) {
        val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
        certificateApiService.getCertificates(userId)
            .enqueue(object : Callback<CertificateResponse> {
                override fun onResponse(
                    call: Call<CertificateResponse>,
                    response: Response<CertificateResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _certificateList.value = response.body()!!.data
                    } else {
                        // 실패 시 빈 리스트 처리하거나 오류 메시지 처리
                        _certificateList.value = emptyList()
                    }
                }

                override fun onFailure(call: Call<CertificateResponse>, t: Throwable) {
                    // 오류 처리 (예: 로그 출력)
                    _certificateList.value = emptyList()
                }
            })
    }
}