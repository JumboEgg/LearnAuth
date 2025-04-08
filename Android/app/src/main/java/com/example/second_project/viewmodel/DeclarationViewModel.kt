package com.example.second_project.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.second_project.data.DeclarationItem
import com.example.second_project.data.ReportItem
import com.example.second_project.data.model.dto.response.ReportApiResponse
import com.example.second_project.network.ApiClient
import com.example.second_project.network.ReportApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "DeclarationViewModel"
class DeclarationViewModel : ViewModel() {
    private val _reportList = MutableLiveData<List<DeclarationItem>>()
    val reportList: LiveData<List<DeclarationItem>> get() = _reportList

    // 신고 목록을 API로부터 가져옵니다.
    fun fetchReports(userID: Int) {
        val reportApiService = ApiClient.retrofit.create(ReportApiService::class.java)
        reportApiService.getReports(userID).enqueue(object : Callback<ReportApiResponse> {
            override fun onResponse(
                call: Call<ReportApiResponse>,
                response: Response<ReportApiResponse>
            ) {
                Log.d(TAG, "onResponse: ${response.body()}")
                if (response.isSuccessful && response.body() != null) {
                    val apiReports = response.body()!!.data
                    // ReportData를 ReportItem으로 매핑합니다.
                    val items = apiReports.map { reportData ->
                        DeclarationItem(
                            reportId = reportData.reportId,
                            title = reportData.title,
                            type = mapReportType(reportData.reportType),
                            content = "" // 목록 API에는 상세 내용이 없으므로 빈 문자열 처리
                        )
                    }
                    _reportList.value = items
                } else {
                    Log.e("DeclarationVM", "Error: ${response.message()}")
                    _reportList.value = emptyList()
                }
            }

            override fun onFailure(call: Call<ReportApiResponse>, t: Throwable) {
                Log.e("DeclarationVM", "API call failed", t)
                _reportList.value = emptyList()
            }
        })
    }

    private fun mapReportType(reportType: Int): String {
        return when (reportType) {
            0 -> "강의자"
            1 -> "강의 자료"
            2 -> "강의 영상"
            else -> "기타"
        }
    }
}
