package com.example.taxi.domain.model.register.car_data

data class CarDataResponse(
    val success: Boolean,
    val status: Int,
    val name: String,
    val message: String,
    val step: Int,
    val data: List<CarData>
)

data class CarData(
    val id: String,
    val name: String,

)
