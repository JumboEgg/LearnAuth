package com.example.second_project.viewmodel

import android.util.Log
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

    fun fetchCertificateDetail(userId: Int, lectureId: Int) {
        Log.d("CertDetailViewModel", "API 호출 시작 - userId: $userId, lectureId: $lectureId")

        // API 요청 URL 로깅
        val baseUrl = ApiClient.retrofit.baseUrl().toString()
        val requestUrl = "$baseUrl/api/certificate/detail?userId=$userId&lectureId=$lectureId"
        Log.d("CertDetailViewModel", "API 요청 URL: $requestUrl")

        val certificateApiService = ApiClient.retrofit.create(CertificateApiService::class.java)
        certificateApiService.getCertificateDetail(userId, lectureId)
            .enqueue(object : Callback<CertificateDetailResponse> {
                override fun onResponse(
                    call: Call<CertificateDetailResponse>,
                    response: Response<CertificateDetailResponse>
                ) {
                    Log.d("CertDetailViewModel", "API 응답 코드: ${response.code()}")
                    Log.d("CertDetailViewModel", "API 응답 메시지: ${response.message()}")

                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            val rawResponse = response.raw().toString()
                            Log.d("CertDetailViewModel", "Raw API 응답: $rawResponse")
                            Log.d("CertDetailViewModel", "API 응답 본문: $responseBody")

                            if (responseBody != null) {
                                _certificateDetail.value = responseBody
                                Log.d("CertDetailViewModel", "데이터 저장 완료: $responseBody")
                            } else {
                                Log.e("CertDetailViewModel", "응답 본문이 null입니다.")
                                _certificateDetail.value = null
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("CertDetailViewModel", "API 응답 실패: ${response.code()} - ${response.message()}")
                            Log.e("CertDetailViewModel", "에러 응답 본문: $errorBody")
                            _certificateDetail.value = null
                        }
                    } catch (e: Exception) {
                        Log.e("CertDetailViewModel", "응답 처리 중 오류 발생: ${e.message}")
                        Log.e("CertDetailViewModel", "스택 트레이스: ${e.stackTraceToString()}")
                        _certificateDetail.value = null
                    }
                }

                override fun onFailure(call: Call<CertificateDetailResponse>, t: Throwable) {
                    Log.e("CertDetailViewModel", "API 호출 실패: ${t.message}")
                    Log.e("CertDetailViewModel", "스택 트레이스: ${t.stackTraceToString()}")
                    _certificateDetail.value = null
                }
            })
    }
}
