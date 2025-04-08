package com.example.second_project.data.model.dto.response

data class CertificateVerifyResponse(
    val success: Boolean,
    val message: String,
    val data: CertificateVerifyData?
)

data class CertificateVerifyData(
    val tokenId: String,
    val studentName: String,
    val lecturerName: String,
    val category: String,
    val issueDate: String,
    val cid: String
) 