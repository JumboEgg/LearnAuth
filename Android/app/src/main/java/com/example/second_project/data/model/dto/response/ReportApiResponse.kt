package com.example.second_project.data.model.dto.response

data class ReportApiResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: List<ReportData>
)

data class ReportData(
    val reportId: Int,
    val title: String,
    val reportType: Int
)