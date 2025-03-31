package com.example.second_project.data.model.dto.response.common

data class ApiResponse<T>(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: T
)