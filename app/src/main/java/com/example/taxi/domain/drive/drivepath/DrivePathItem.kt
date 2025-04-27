package com.example.taxi.domain.drive.drivepath

import com.example.taxi.domain.location.LocationPoint
import com.google.gson.annotations.SerializedName

data class DrivePathItem(
    @SerializedName("locationPoint") val locationPoint: LocationPoint,
    @SerializedName("speed") val speed: Float,
    @SerializedName("time") val time: Long,
    @SerializedName("duration") val duration: Long,
    @SerializedName("distance") val distance: Int,
    @SerializedName("nextLocationPoint") val nextLocationPoint: LocationPoint?
)