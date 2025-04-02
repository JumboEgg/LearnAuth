package com.example.second_project.data.model.dto.request

import com.example.second_project.data.model.dto.response.SubLecture

data class Lecture(
    val lectureId: Int,
    val title: String,
    val price: Int,
    val lecturer: String?,    // 필요에 따라 non-null로 변경 가능
    val lectureUrl: String?,
    val categoryName: String,
    val subLectures: List<SubLecture>? = null
)