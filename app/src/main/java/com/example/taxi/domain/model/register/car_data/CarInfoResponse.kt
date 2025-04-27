package com.example.taxi.domain.model.register.car_data

class CarInfoResponse(
        val success: Boolean,
        val status: Int,
        val name: String,
        val message: String,
        val step: Int,
        val data: DataModel
    )

    data class DataModel(
        val driver_id: Int,
        val car_id: Int,
        val tech_pass_number: String,
        val car_number: String,
        val color_id: Int,
        val position: Int,
        val accepted: Any?, // Change the type to the appropriate one if not nullable
        val accepted_by: Any?, // Change the type to the appropriate one if not nullable
        val status: Any?, // Change the type to the appropriate one if not nullable
        val created_at: Long,
        val updated_at: Long,
        val created_by: Int,
        val updated_by: Int,
        val id: Int
    )

