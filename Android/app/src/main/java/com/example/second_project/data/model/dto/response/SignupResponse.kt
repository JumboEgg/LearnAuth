package com.example.second_project.data.model.dto.response

data class SignupResponse(
    val timeStamp: String,
    val code: Int,
    val status: String?,
    val data: SignupData
)

data class SignupData(
    val nickname: String,
    val message: String
)