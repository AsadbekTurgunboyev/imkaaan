package com.example.taxi.domain.drive.currentDrive

import android.util.Log
import com.example.taxi.domain.drive.Drive
import com.example.taxi.domain.drive.drivepath.DrivePathBuilder
import com.example.taxi.domain.drive.drivepath.DrivePathItem
import com.example.taxi.domain.location.LocationPoint
import com.example.taxi.domain.preference.UserPreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit
class CurrentDrive {

    private val sphericalUtil: com.example.taxi.utils.SphericalUtil by lazy {
        GlobalContext.get().get()
    }

    private val drivePathBuilder: DrivePathBuilder by lazy {
        GlobalContext.get().get()
    }

    private val endForgotCalculator: EndForgotCalculator by lazy {
        GlobalContext.get().get()
    }

    private val pauseCalculator: PauseCalculator by lazy {
        GlobalContext.get().get()
    }

    private val userPreferenceManager by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var lastSavedDistance: Int = 0
    private val gson = Gson()
    private var startLocation: LocationPoint? = null
    private var distance: Int = 0
    private var startTime: Long = 0L
    private var topSpeed: Float = 0f
    private var lastPingTime = startTime
    private var lastLocation = startLocation
    private var currentSpeed: Float = 0f
    private var status: Int = CurrentDriveStatus.STARTING
    private var pauseJob: Job? = null
    private var pauseTime: Int = 0
    init {
        loadCurrentDrive()
    }

    fun pingData(
        locationTime: Long,
        currentLat: Double,
        currentLon: Double,
        gpsSpeed: Float?,
    ) {
        status = CurrentDriveStatus.STARTED
        // Calculate the distance in meters between the new location and the last location

        if (startLocation == null) {
            initialize(locationTime, currentLat, currentLon)
        }
        val prevLocationNonNull = lastLocation ?: return
        val currentLocation = LocationPoint(currentLat, currentLon)
        val distanceInMetre = getDistanceInMetre(prevLocationNonNull, currentLat, currentLon)
        val timeTakenInSecs = TimeUnit.MILLISECONDS.toSeconds(locationTime - lastPingTime)


        if (pauseCalculator.considerPingForStopPause(locationTime)) {
            lastLocation = currentLocation
            lastPingTime = locationTime
            endForgotCalculator.addPoint(locationTime, LocationPoint(currentLat, currentLon))
            return
        }
        endForgotCalculator.addPoint(locationTime, LocationPoint(currentLat, currentLon))
        if (timeTakenInSecs > 1 && locationTime > lastPingTime) {
            currentSpeed = gpsSpeed ?: 0f
            val spd = distanceInMetre.toDouble() / timeTakenInSecs.toDouble()

            if (spd == 0.0) {
                currentSpeed = 0f
            } else if (currentSpeed == 0f) {
                currentSpeed = spd.toFloat()
            } else if (spd / currentSpeed > 1 && spd / currentSpeed < 1.2) {
                //gps speed is similar to computed
                currentSpeed = spd.toFloat()
            } else if (spd / currentSpeed > 1 && spd / currentSpeed < 2) {
                //Take average of gps speed and computed speed.
                currentSpeed = (spd.toFloat() + currentSpeed) / 2
            }

            if (currentSpeed > topSpeed) {
                topSpeed = currentSpeed
            }

            if (endForgotCalculator.isForgot(currentSpeed).not()) {
                distance += distanceInMetre
                drivePathBuilder.addRacePathItem(currentLocation, currentSpeed, locationTime)

                lastLocation = currentLocation
                lastPingTime = locationTime
            }
        }

        if (distance - lastSavedDistance >= 70) {
            coroutineScope.launch {
                saveCurrentDrive()
            }
            lastSavedDistance = distance
        }
    }

    private fun loadCurrentDrive() {
        val currentDriveJson = userPreferenceManager.getCurrentDrive() ?: return
        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val currentDriveData: Map<String, Any> = gson.fromJson(currentDriveJson, type)
            startLocation = gson.fromJson(
                currentDriveData["startLocation"] as String,
                LocationPoint::class.java
            )
            pauseTime = (currentDriveData["pauseTime"] as Double).toInt()
            distance = (currentDriveData["distance"] as Double).toInt()
            startTime = (currentDriveData["startTime"] as Double).toLong()
            topSpeed = (currentDriveData["topSpeed"] as Double).toFloat()
            lastPingTime = (currentDriveData["lastPingTime"] as Double).toLong()
            lastLocation = gson.fromJson(currentDriveData["lastLocation"] as String, LocationPoint::class.java)
            currentSpeed = (currentDriveData["currentSpeed"] as Double).toFloat()
            status = (currentDriveData["status"] as Double).toInt()
        } catch (e: JsonSyntaxException) {
            println("Error loading current drive: ${e.message}")
        }
    }

    fun getAverageSpeed(): Double {
        val timeTaken = getTotalTime()

        if (distance > 20 && timeTaken > TimeUnit.SECONDS.toMillis(1)) {
            return (distance.toDouble() / (TimeUnit.MILLISECONDS.toSeconds(timeTaken).toDouble()))
        }

        return 0.0
    }

    fun getTotalTime() = (lastPingTime - startTime) - pauseCalculator.getPausedTime(lastPingTime,pauseTime)


    fun getPauseTime(): Long {
        return if (startTime > 0) pauseCalculator.getPausedTime(
            System.currentTimeMillis(),pauseTime
        ) else 0
    }

    fun getRunningTime(): Long {
        return if (startTime > 0) (System.currentTimeMillis() - startTime) - pauseCalculator.getPausedTime(
            System.currentTimeMillis(),pauseTime
        ) else 0
    }

    fun getCurrentSpeed() = currentSpeed

    fun getTopSpeed() = topSpeed

    fun getDistance() = distance

    fun getStatus() = status

    fun isStopped() = status == CurrentDriveStatus.STOPPED

    fun isPaused() = status == CurrentDriveStatus.PAUSED

    fun isStarting() = status == CurrentDriveStatus.STARTING

    fun onStart() {
        this.status = CurrentDriveStatus.STARTING
        pauseJob?.cancel()

        coroutineScope.launch {
            saveCurrentDrive()
        }
    }

    fun onPause() {
        this.status = CurrentDriveStatus.PAUSED
        pauseCalculator.onPause()
        Log.d("saqlash", "onPause: ")
        lastLocation?.let {
            drivePathBuilder.addRacePathItem(it, currentSpeed, lastPingTime, true)
        }

        pauseJob?.cancel()

        // Har 10 soniyada saveCurrentDrive() ni chaqirish uchun interval
        pauseJob = coroutineScope.launch {
            while (status == CurrentDriveStatus.PAUSED) {
                Log.d("saqlash", "onPause: 10soniya")
                saveCurrentDrive()
                delay(10000) // 10 soniya
            }
        }
    }

    fun onStop() {
        this.status = CurrentDriveStatus.STOPPED
        lastLocation?.let {
            drivePathBuilder.addRacePathItem(it, currentSpeed, lastPingTime, true)
        }
        pauseJob?.cancel()
        coroutineScope.launch {
            saveCurrentDrive()
        }
    }

    private fun saveCurrentDrive() {
        val currentDriveData = mapOf(
            "startLocation" to gson.toJson(startLocation),
            "distance" to distance,
            "startTime" to startTime,
            "topSpeed" to topSpeed,
            "lastPingTime" to lastPingTime,
            "lastLocation" to gson.toJson(lastLocation),
            "currentSpeed" to currentSpeed,
            "status" to status,
            "pauseTime" to pauseCalculator.getPausedTime(lastPingTime,pauseTime)
        )
        userPreferenceManager.saveCurrentDrive("currentDrive", gson.toJson(currentDriveData))
    }

    fun getAsDrive(): Drive? {
        val startLocationNonNull = startLocation ?: return null
        val endLocationNonNull = lastLocation ?: return null
        if (lastPingTime == 0L) return null
        if (getTotalTime() < 5L) return null
        if (distance < 1) return null

        return Drive(
            startLocation = startLocationNonNull,
            endLocation = endLocationNonNull,
            distance = distance,
            startTime = startTime,
            endTime = lastPingTime,
            pauseTime = pauseCalculator.getPausedTime(lastPingTime,pauseTime),
            topSpeed = topSpeed.toDouble(),
            locationPath = drivePathBuilder.getRacePathLite()
        )
    }

    fun getRacePath(): List<DrivePathItem> {
        return drivePathBuilder.getRacePath()
    }

    private fun initialize(
        locationTime: Long,
        currentLat: Double,
        currentLon: Double
    ) {
        this.startTime = locationTime
        startLocation = LocationPoint(
            latitude = currentLat,
            longitude = currentLon
        )
        lastLocation = startLocation
    }

    private fun getDistanceInMetre(
        prevLocation: LocationPoint,
        currentLat: Double,
        currentLon: Double
    ): Int {
        return sphericalUtil.computeDistanceBetween(
            LatLng(
                prevLocation.latitude, prevLocation.longitude
            ), LatLng(currentLat, currentLon)
        ).toInt()
    }
}