package com.example.second_project.data

data class ReportItem(
    val reportId: Int,
    val title: String,
    val type: String,
    val content: String // 상세정보는 API 호출 후 채워질 수 있음.
)
