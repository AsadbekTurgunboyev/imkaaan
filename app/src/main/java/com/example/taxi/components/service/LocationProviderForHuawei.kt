package com.example.taxi.components.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.core.context.GlobalContext

class LocationProviderForHuawei(val context: Context) : LocationListener, GnssStatus.Callback() {


    private var locationChangeCallback: (Location) -> Unit = {}
    private var gpsSignalCallback: (signalStrength: Int) -> Unit = {}
    private var startTime = System.currentTimeMillis()
    private var currentGPSStrength = 0

    private val locationManager: LocationManager by lazy {
        GlobalContext.get().get()
    }

    override fun onLocationChanged(location: Location) {
        if (isValidLocation(location)) {
            locationChangeCallback(location)
        } else {
            getLastKnownLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribe(
        locationChangeCallback: (Location) -> Unit,
        gpsSignalCallback: (gpsSignalStrength: Int) -> Unit
    ) {
        this.locationChangeCallback = locationChangeCallback
        this.gpsSignalCallback = gpsSignalCallback
//        getLastKnownLocation()
        startTime = System.currentTimeMillis()


        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
            isCostAllowed = true // This allows Android to use data services
        }

//        if (isGooglePlayServicesAvailable()) {
//            Log.d("tekshirish1", "subscribe: play market bot")
////            startFusedLocationUpdates()
//        } else {
//            Log.d("tekshirish1", "yoq")
//
////            startLegacyLocationUpdates()
//        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0F, this)
        val isSuccess = locationManager.registerGnssStatusCallback(
            this,
            Handler(Looper.getMainLooper())
        )
        Log.d("GPS", "GnssStatus Callback registration successful: $isSuccess")
    }

    private fun isGPSEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun getLastKnownLocation() {
        try {
            val lastKnownLocation: Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocation?.let {
                locationChangeCallback(it)
            }
        } catch (e: SecurityException) {
            Log.e("masofa", "Ruxsatlar mavjud emas: ${e.message}")
        }
    }

    fun unsubscribe() {
        this.locationChangeCallback = {}
        locationManager.removeUpdates(this)
    }

    override fun onProviderEnabled(provider: String) {
        //Nothing to do
    }

    override fun onProviderDisabled(provider: String) {
        //Nothing to do
    }

    override fun onSatelliteStatusChanged(status: GnssStatus) {
        super.onSatelliteStatusChanged(status)
        status.let { gnsStatus ->
            val totalSatellites = gnsStatus.satelliteCount
            if (totalSatellites > 0) {
                var satellitesFixed = 0
                for (i in 0 until totalSatellites) {
                    if (gnsStatus.usedInFix(i)) {
                        satellitesFixed++
                    }
                }
                currentGPSStrength = (satellitesFixed * 100) / totalSatellites
                gpsSignalCallback(currentGPSStrength)
            }
        }
    }

    private fun isValidLocation(location: Location): Boolean {


        if (currentGPSStrength == 0) {
            return false
        }

        if (location.accuracy <= 0 || location.accuracy > 40) {
            return false
        }
        return true
    }
}