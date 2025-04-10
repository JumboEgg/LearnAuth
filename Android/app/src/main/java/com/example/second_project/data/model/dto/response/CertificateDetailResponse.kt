package com.example.second_project.data.model.dto.response

data class CertificateDetailResponse(
    val timeStamp: String,
    val code: Int,
    val status: String,
    val data: CertificateDetailData
)

data class CertificateDetailData(
    val title: String,
    val teacherName: String,
    val teacherWallet: String,
    val certificateDate: String,
    val certificate: Int?,
    val qrCode: String?
)