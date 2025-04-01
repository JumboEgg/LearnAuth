//package com.example.second_project.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.example.second_project.data.ReportItem
//import com.example.second_project.data.model.dto.response.ReportApiResponse
//import com.example.second_project.network.ApiClient
//import com.example.second_project.network.ReportApiService
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class DeclarationViewModel : ViewModel() {
//
//    private val _reportList = MutableLiveData<List<ReportItem>>()
//    val reportList: LiveData<List<ReportItem>> = _reportList
//
//    // 샘플 데이터
//
//    fun fetchReports(userID: Int) {
//        val reportApiService = ApiClient.retrofit.create(ReportApiService::class.java)
//        reportApiService.getReports(userID).enqueue(object : Callback<ReportApiResponse> {
//            override fun onResponse(
//                call: Call<ReportApiResponse>,
//                response: Response<ReportApiResponse>
//            ) {
//                if (response.isSuccessful && response.body() != null) {
//                    val apiReports = response.body()!!.data
//                    val items = apiReports.map { reportData ->
//                        ReportItem(
//                            reportId = reportData.reportId, // 수정: reportId 필드 추가
//                            title = reportData.title,
//                            type = mapReportType(reportData.reportType),
//                            content = ""
//                        )
//                    }
//                    _reportList.value = items
//                }
//            }
//
//            override fun onFailure(call: Call<ReportApiResponse>, t: Throwable) {
//
//            }
//        })
//    }
//
//    private fun mapReportType(reportType: Int): String {
//        return when (reportType) {
//            0 -> "강의자"
//            1 -> "강의 자료"
//            2 -> "강의 영상"
//            else -> "기타"
//        }
//    }
//}
