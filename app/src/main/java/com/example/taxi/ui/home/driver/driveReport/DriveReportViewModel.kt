package com.example.taxi.ui.home.driver.driveReport

import androidx.lifecycle.*
import com.example.taxi.domain.drive.DriveLocalityAddService
import com.example.taxi.domain.drive.DriveStatAnalyser
import com.example.taxi.domain.drive.drivepath.DrivePathFilter
import com.example.taxi.domain.drive.drivepath.DrivePathItem
import com.example.taxi.domain.location.LocationPoint
import com.example.taxi.repositeries.DriveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DriveReportViewModel(
    private val driveRepository: DriveRepository,
    private val drivePathFilter: DrivePathFilter,
    private val driveLocalityAddService: DriveLocalityAddService,
    private val driveStatAnalyser: DriveStatAnalyser,
) : ViewModel() {
    private var driveId: Long = -1L

    fun setInitData(driveId: Long) {
        this.driveId = driveId
    }

    fun getDrivePath(): LiveData<List<LocationPoint>>{
       return driveRepository.getDriveLiveDataPath(driveId).switchMap {
          liveData(context = viewModelScope.coroutineContext + Dispatchers.Default){
              it?.let { it1 -> emit(it1) }
          }
       }
    }

    fun getDriveAnalyticsLiveData(): LiveData<DriveAnalyticsData> {
        return driveRepository.getDriveLiveData(driveId).switchMap { drive ->

            liveData(context = viewModelScope.coroutineContext + Dispatchers.Default) {

                val driveAnalyticsData = drive?.let {
                    val drivePath: List<DrivePathItem> =
                        drivePathFilter.filter(driveRepository.getDrivePath(driveId) ?: emptyList())

                    val driveStatsData =
                        driveStatAnalyser.getDriveStatsData(drivePath)

                    val speedMap = driveStatAnalyser.getDriveSpeedMap(drivePath)

                    DriveAnalyticsData(
                        driveTag = drive.tag,
                        startLocality = drive.startLocality,
                        endLocality = drive.endLocality,
                        noOfStops = driveStatsData.noOfStops,
                        idleTime = driveStatsData.idleTime,
                        pauseTime = drive.pauseTime,
                        distance = drive.distance,
                        startTime = drive.startTime,
                        endTime = drive.endTime,
                        topSpeed = drive.topSpeed,
                        speedMap = speedMap
                    )
                } ?: DriveAnalyticsData.empty()

                emit(driveAnalyticsData)
            }
        }
    }
    fun checkAndUpdateDrive() {
        viewModelScope.launch(Dispatchers.IO) {
            driveRepository.getDrive(driveId)?.let { drive ->
                if (drive.startLocality == null || drive.endLocality == null) {
                    driveLocalityAddService.getAndUpdateLocality(drive)
                }
            }
        }
    }

    fun deleteDrive(driveId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            driveRepository.deleteRace(driveId)
        }
    }
}