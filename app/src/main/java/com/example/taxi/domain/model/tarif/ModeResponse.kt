package com.example.taxi.domain.model.tarif

data class ModeResponse(
    val success: Boolean,
    val status: Int,
    val name: String,
    val message: String,
    val step: Int,
    val data: List<Mode>
)

data class Mode(
    val id: String,
    val name: String,
    val value: String,
    val cost: String
)
