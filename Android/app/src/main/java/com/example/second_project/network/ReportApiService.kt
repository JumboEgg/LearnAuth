package com.example.second_project.network

import com.example.second_project.data.ReportItem
import com.example.second_project.data.model.dto.response.ReportApiResponse
import com.example.second_project.data.model.dto.response.ReportDetailResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportApiService {
    @GET("/api/report")
    fun getReports(@Query("userId") userId : Int) : Call<ReportApiResponse>

    @GET("/api/report/{reportId}")
    fun getReportDetail(@Path("reportId") reportId : Int) : Call<ReportDetailResponse>

    @POST("/api/report")
    fun postReport(@Body report: ReportItem) : Call<ReportApiResponse>
}