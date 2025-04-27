package com.example.taxi.domain.model.register.car_data

class CarColorResponse (
    val success: Boolean,
    val status: Int,
    val name: String,
    val message: String,
    val step: Int,
    val data: List<CarColorData>
    )

    data class CarColorData(
        val id: String,
        val name: String,
        val code: String
    )
