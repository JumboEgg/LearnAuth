package com.example.second_project.data.model.dto.response

import com.example.second_project.data.model.dto.request.Lecture

data class LectureSearchResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: LectureSearchData
)

data class LectureSearchData(
    val totalResults: Int,
    val currentPage: Int,
    val searchResults: List<Lecture>
)
