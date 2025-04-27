package com.example.taxi.domain.drive

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.taxi.components.service.LocationProvider
import com.example.taxi.components.service.LocationProviderForHuawei
import com.example.taxi.domain.drive.currentDrive.CurrentDrive
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.repositeries.DriveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class DriveService {

    private var currentDrive: CurrentDrive? = null
    private var dashboardDataCallback: (DashboardData) -> Unit = {}
    private var driveFinishCallback: (Long?) -> Unit = {}
    private var gpsSignalStrength = 0

    private val locationProvider by lazy {
        GlobalContext.get().get<LocationProvider>()
    }

    private val locationProviderForHuawei by lazy {
        GlobalContext.get().get<LocationProviderForHuawei>()
    }

    private val driveRepository by lazy {
        GlobalContext.get().get<DriveRepository>()
    }

    private val userPreferenceManager by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }

    fun registerCallback(
        dashboardDataCallback: (DashboardData) -> Unit,
        driveFinishCallback: (Long?) -> Unit
    ) {
        this.dashboardDataCallback = dashboardDataCallback
        this.driveFinishCallback = driveFinishCallback
    }

    fun startDrive() {
        if (currentDrive == null || currentDrive?.isStopped() == true) {
            currentDrive = CurrentDrive()
        }
        currentDrive?.onStart()
        if (isHuaweiDevice()) {
            locationProviderForHuawei.subscribe(
                locationChangeCallback = { currentLocation ->
                    currentDrive?.pingData(
                        locationTime = currentLocation.time,
                        currentLat = currentLocation.latitude,
                        currentLon = currentLocation.longitude,
                        gpsSpeed = currentLocation.speed,
                    )
                    dashboardDataCallback(getDashboardData())
                },
                gpsSignalCallback = { gpsSignalStrength ->
                    this.gpsSignalStrength = gpsSignalStrength
                    if (currentDrive?.isStarting() == true) {
                        dashboardDataCallback(getDashboardData())
                    }
                }
            )
            dashboardDataCallback(getDashboardData())
        } else {
            locationProvider.subscribe(
                locationChangeCallback = { currentLocation ->
                    currentDrive?.pingData(
                        locationTime = currentLocation.time,
                        currentLat = currentLocation.latitude,
                        currentLon = currentLocation.longitude,
                        gpsSpeed = currentLocation.speed,
                    )
                    dashboardDataCallback(getDashboardData())
                },
                gpsSignalCallback = { gpsSignalStrength ->
                    this.gpsSignalStrength = gpsSignalStrength
                    if (currentDrive?.isStarting() == true) {
                        dashboardDataCallback(getDashboardData())
                    }
                }
            )
            dashboardDataCallback(getDashboardData())
        }
    }

    fun pauseDrive() {
        currentDrive?.onPause()
        locationProvider.unsubscribe()
        dashboardDataCallback(getDashboardData())
    }

    fun stopDrive() {
        locationProvider.unsubscribe()
        currentDrive?.onStop()
        dashboardDataCallback(DashboardData.empty())
        currentDrive?.let { drive ->
            addDriveAndTriggerCallback(drive)
        }
        currentDrive = null
    }

    private fun addDriveAndTriggerCallback(currentDrive: CurrentDrive) =
        CoroutineScope(Dispatchers.IO).launch {
            addCurrentDriveAsDrive(currentDrive).let { driveId ->
                CoroutineScope(Dispatchers.Main).launch {
                    driveFinishCallback(driveId)
                }
            }
        }

    private suspend fun addCurrentDriveAsDrive(currentDrive: CurrentDrive): Long? {

        return currentDrive.getAsDrive()?.let {
            driveRepository.addDrive(it, currentDrive.getRacePath())
        }
    }

    fun isRaceOngoing() = currentDrive != null


    fun getDashboardData(): DashboardData {
        return currentDrive?.let {
            DashboardData(
                runningTime = it.getRunningTime(), // vaqt qoshish kerak ertaga qilaman. Preferencedan olaman
                pauseTime = it.getPauseTime(),
                currentSpeed = it.getCurrentSpeed().toDouble(),
                topSpeed = it.getTopSpeed().toDouble(),
                averageSpeed = it.getAverageSpeed(),
                distance = it.getDistance(),
                status = it.getStatus(),
                gpsSignalStrength = gpsSignalStrength
            )
        } ?: DashboardData.empty()
    }


    private fun isHuaweiDevice(): Boolean {
        return Build.MANUFACTURER.equals("Huawei", ignoreCase = true)
    }
}