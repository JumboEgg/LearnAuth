package com.example.second_project.data

data class ReportItem(
    val title: String,      // 강의 제목
    val type: String,       // 신고 부류 (ex: 강의 자료, 강의 영상, ...)
    val content: String     // 신고 상세 내용
)
