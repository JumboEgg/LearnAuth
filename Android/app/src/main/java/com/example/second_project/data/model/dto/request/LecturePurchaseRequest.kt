package com.example.second_project.data.model.dto.request

import com.example.second_project.blockchain.SignedRequest

/**
 * 강의 구매 요청 데이터 클래스
 */
data class LecturePurchaseRequest(
    val request: PurchaseRequest,
    val signedRequest: SignedRequest
)

/**
 * 구매 요청 기본 정보
 */
data class PurchaseRequest(
    val userId: Int,
    val lectureId: Int
) 