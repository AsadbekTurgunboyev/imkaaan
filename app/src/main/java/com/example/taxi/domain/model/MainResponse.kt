package com.example.taxi.domain.model

data class MainResponse<T>(
    val success: Boolean,
    val status: Int,
    val name: String,
    val message: String,
    val step: Int,
    var data: T
)

data class BonusResponse(
    val order_history_id: Int
)

data class PaymentUrl(
    val url: String
)
