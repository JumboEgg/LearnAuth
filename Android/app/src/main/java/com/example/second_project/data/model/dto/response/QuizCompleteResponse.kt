package com.example.second_project.data.model.dto.response

data class QuizCompleteResponse(
    val timeStamp: String,
    val code: Int,
    val status: String?,
    val data: Boolean
) 