package com.example.taxi.components.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.taxi.utils.LocationPermissionUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.mapbox.common.core.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.LinkedList


class LocationProvider(
    private val context: Context
) : KoinComponent {

    private var locationChangeCallback: (Location) -> Unit = {}
    private var gpsSignalCallback: (signalStrength: Int) -> Unit = {}
    private val fusedLocationClient: FusedLocationProviderClient by inject()
    private val lastLocations = LinkedList<Location>()
    private var lastValidLocation: Location? = null
    private var gpsSignalStrength = 0
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1000L
    )
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(700)
        .setMaxUpdateDelayMillis(2000)
        .build()

    companion object {
        private const val MAX_DISTANCE_METERS = 700
        private const val MAX_ACCURACY_METERS = 50f
        private const val MAX_LOCATION_HISTORY = 4
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val signalStrength = (0 until status.satelliteCount)
                .filter { status.usedInFix(it) }
                .map { status.getCn0DbHz(it) }
                .average()
                .toInt() ?: 0

            gpsSignalCallback(signalStrength)
            gpsSignalStrength = signalStrength
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { rawLocation ->
                if (isValidLocation(rawLocation)) {
                    locationChangeCallback(rawLocation)
                    addLocationToHistory(rawLocation)
                }
            }
        }
    }


    fun subscribe(
        locationChangeCallback: (Location) -> Unit,
        gpsSignalCallback: (gpsSignalStrength: Int) -> Unit
    ) {
        this.locationChangeCallback = locationChangeCallback
        this.gpsSignalCallback = gpsSignalCallback
        startGpsSignalTracking()
        startFusedLocationUpdates()
        fetchLastKnownLocation()
    }


    @SuppressLint("MissingPermission")
    private fun startFusedLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun fetchLastKnownLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    LocationPermissionUtils.isLocationEnabled(context)
                } else {
                    val location = fusedLocationClient.lastLocation.await()
                    location?.let {
                        lastValidLocation = it
                        locationChangeCallback(it)
                    }
                }


            } catch (e: Exception) {
                logDebug("jarayonn", "Error fetching last location: ${e.message}")
            }
        }
    }

    private fun addLocationToHistory(location: Location) {
        if (lastLocations.size >= MAX_LOCATION_HISTORY) {
            lastLocations.removeFirst()
        }
        lastLocations.add(location)
    }


    fun unsubscribe() {
        stopGpsSignalTracking()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        this.locationChangeCallback = {}
        this.gpsSignalCallback = {}
    }

    private fun isValidLocation(location: Location): Boolean {
//        if (lastLocations.isEmpty()) return location.accuracy <= MAX_ACCURACY_METERS
//
//        val averageDistance = lastLocations.map { it.distanceTo(location) }.average().toFloat()
//        val averageAccuracy = lastLocations.map { it.accuracy }.average().toFloat()
//
//        return when {
//
//            averageDistance > MAX_DISTANCE_METERS && location.accuracy <= MAX_ACCURACY_METERS && gpsSignalStrength > 30 -> true
//
//            averageDistance <= MAX_DISTANCE_METERS && averageAccuracy <= MAX_ACCURACY_METERS -> true
//
//            else -> false

//        }
        return location.accuracy <= MAX_ACCURACY_METERS
    }

    @SuppressLint("MissingPermission")
    private fun startGpsSignalTracking() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val handler = Handler(Looper.getMainLooper())
        locationManager.registerGnssStatusCallback(gnssStatusCallback, handler)
    }


    private fun stopGpsSignalTracking() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
    }

    private fun logDebug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }



}

