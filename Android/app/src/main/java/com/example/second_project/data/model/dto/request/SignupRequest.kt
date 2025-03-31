package com.example.second_project.data.model.dto.request

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val wallet: String,
    val name: String
)
