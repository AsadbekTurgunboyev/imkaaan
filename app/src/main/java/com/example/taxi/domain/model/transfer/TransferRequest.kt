package com.example.taxi.domain.model.transfer

data class TransferRequest(
    val to_id: Int,
    val amount: Int
)
