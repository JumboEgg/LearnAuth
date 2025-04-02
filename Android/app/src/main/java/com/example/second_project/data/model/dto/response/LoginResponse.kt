package com.example.second_project.data.model.dto.response

data class LoginResponse(
    val code: Int,
    val `data`: Data,
    val status: String,
    val timeStamp: String
)

data class Data(
    val certificateCount: Int,
    val nickname: String,
    val userId: Int,
    val wallet: String,
    val name : String
)