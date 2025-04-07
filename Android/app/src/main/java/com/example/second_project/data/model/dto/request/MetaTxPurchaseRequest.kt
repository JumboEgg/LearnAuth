package com.example.second_project.data.model.dto.request

import java.math.BigInteger

/**
 * 강의 구매 요청 데이터 클래스
 */
data class PurchaseRequest(
    val userId: Int,
    val lectureId: Int,
    val approveRequest: SignedRequestDto,
    val purchaseRequest: SignedRequestDto
)

data class ForwardRequestDto(
    val from: String,
    val to: String,
    val value: BigInteger,
    val gas: BigInteger,
    val nonce: BigInteger,
    val deadline: BigInteger,
    val data: String
)

data class SignedRequestDto(
    val request: ForwardRequestDto,
    val signature: String
)
