package com.example.taxi.domain.model.location

data class LocationRequest(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Int,
    val angle: Int
)

