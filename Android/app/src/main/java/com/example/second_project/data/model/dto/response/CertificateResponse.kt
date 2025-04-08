package com.example.second_project.data.model.dto.response

data class CertificateResponse(
    val timeStamp : String,
    val code : Int,
    val status : String,
    val data : List<CertificateData>
)

data class CertificateData(
    val lectureId: Int,
    val title: String,
    val categoryName: String,
    val certificate: Int,
    val certificateDate: String
)

