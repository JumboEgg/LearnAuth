package com.example.second_project.data.model.dto.response

data class CertificateIssueResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: CertificateIssueData
)

data class CertificateIssueData(
    val token: String
) 