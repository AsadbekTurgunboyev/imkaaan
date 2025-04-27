package com.example.taxi.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

class LocationTracker private constructor(private val context: Context) {

    interface LocationUpdateListener {
        fun onLocationChanged(location: Location?)
        fun onDistanceChanged(distance: Float)
    }

    private var lastLocation: Location? = null
    private var pausedLocation: Location? = null
    private var locationUpdateListener: LocationUpdateListener? = null
    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var isFirst = true


    private val locationListener: LocationListener = LocationListener { location ->
        location.let {
            if (isFirst) {
                pausedLocation = it
                isFirst = false
            }
            if (pausedLocation != null) {
                val distance = pausedLocation!!.distanceTo(it)

                 if (distance > 80) {
                    isFirst = true
                    locationUpdateListener?.onDistanceChanged(distance)
                    stopLocationUpdates()
                }
            }
            lastLocation = it
            locationUpdateListener?.onLocationChanged(it)
        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdates(listener: LocationUpdateListener) {
        this.locationUpdateListener = listener
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
        this.locationUpdateListener = null
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LocationTracker? = null

        fun getInstance(context: Context): LocationTracker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationTracker(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun pauseTracking() {
        pausedLocation = lastLocation
//        stopLocationUpdates()
    }

    fun resumeTracking(listener: LocationUpdateListener) {
        this.locationUpdateListener = listener
        startLocationUpdates(listener)
    }
}


