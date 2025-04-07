package com.example.second_project.data.model.dto.request

import java.math.BigInteger

data class DepositRequest(
    val userId: Int,
    val quantity: BigInteger
)
