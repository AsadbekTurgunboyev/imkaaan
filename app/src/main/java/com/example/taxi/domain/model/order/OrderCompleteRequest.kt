package com.example.taxi.domain.model.order

import com.example.taxi.domain.location.LocationPoint

data class OrderCompleteRequest(
    val cost: Int,
    val distance: Int,
    val wait_time: Int,
    val wait_cost: Int,
    val coordinates: List<LocationPoint>
)
