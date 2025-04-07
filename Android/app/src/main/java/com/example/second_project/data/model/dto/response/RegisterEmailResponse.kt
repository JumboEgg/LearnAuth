package com.example.second_project.data.model.dto.response

data class RegisterEmailResponse(
    val email: String,
    val nickname: String,
    val name: String
)

data class EmailSearchData(
    val totalResults: Int,
    val currentPage: Int,
    val searchResults: List<RegisterEmailResponse>
)
