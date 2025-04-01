package com.example.second_project.data.model.dto.response

import com.example.second_project.data.model.dto.request.Lecture

data class RandomLecturesResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: List<Lecture>
)