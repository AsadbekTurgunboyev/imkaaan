package com.example.taxi.domain.location

import com.google.gson.annotations.SerializedName

data class LocationPoint(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)