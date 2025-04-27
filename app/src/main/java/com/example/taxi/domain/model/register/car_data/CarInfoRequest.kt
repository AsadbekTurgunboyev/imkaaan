package com.example.taxi.domain.model.register.car_data

data class CarInfoRequest(
    val car_id: Int,
    val position: Int,
    val tech_pass_number: String,
    val car_number: String,
    val color_id: Int
)
