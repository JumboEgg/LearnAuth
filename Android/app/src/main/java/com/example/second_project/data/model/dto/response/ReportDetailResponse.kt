package com.example.second_project.data.model.dto.response

data class ReportDetailResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: ReportDetailData
)

data class ReportDetailData(
    val title: String,
    val reportType: Int,
    val reportDetail: String
)
