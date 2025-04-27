package com.example.taxi.domain.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.taxi.domain.drive.currentDrive.CurrentDriveStatus.PAUSED
import com.example.taxi.domain.drive.currentDrive.CurrentDriveStatus.STOPPED
import com.example.taxi.utils.ClockUtils
import com.example.taxi.utils.ConversionUtil
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.koin.core.context.GlobalContext

@Parcelize
data class DashboardData(
    private val currentSpeed: Double,
    private val topSpeed: Double,
    private val averageSpeed: Double,
    val distance: Int,
    private val runningTime: Long,
    private val pauseTime: Long,
    private val status: Int,
    private val gpsSignalStrength: Int,
) : Parcelable {

    @IgnoredOnParcel
    private val conversionUtil by lazy {
        GlobalContext.get().get<ConversionUtil>()
    }

    @IgnoredOnParcel
    private val clockUtils by lazy {
        GlobalContext.get().get<ClockUtils>()
    }

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    fun getCurrentSpeed() = conversionUtil.getSpeed(currentSpeed).toInt()

    fun getTopSpeed() = conversionUtil.getSpeed(topSpeed).toInt()

    fun getAverageSpeedText() = conversionUtil.getSpeedStr(averageSpeed)

    fun getCurrentSpeedText() = conversionUtil.getSpeedStr(currentSpeed)

    fun getTopSpeedText() = conversionUtil.getSpeedStr(topSpeed)

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistanceText() = conversionUtil.getDistance(distance.toDouble())


    @RequiresApi(Build.VERSION_CODES.N)
    fun convertToKm(metr: Int) = conversionUtil.getDistance(metr.toDouble())
    @RequiresApi(Build.VERSION_CODES.N)
    fun getSpeedUnitText() = conversionUtil.getSpeedUnit()

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistanceUnitText() = conversionUtil.getDistanceUnit()

    fun getGPSSignalStrength() = gpsSignalStrength

    fun timeText(): String {
        if (runningTime == 0L || runningTime <= 0L) {
            return "00:00"
        }

        return clockUtils.getTimeFromMillis(runningTime)
    }

    fun getPauseTimeNormal(): Long{
        return pauseTime
    }


    fun getPauseTime(): Int{
        return (pauseTime / 1000).toInt()
    }

    fun getStatus(): Int {
        return status
    }

    fun getTime(): Int{
        return (runningTime / 1000).toInt()
    }
    fun isRunning(): Boolean {
        return status != STOPPED
    }

    fun getRunningTime(): Long{
        return runningTime
    }
    fun isPaused(): Boolean {
        return status == PAUSED
    }

    companion object {
        fun empty() = DashboardData(
            runningTime = 0L,
            pauseTime = 0L,
            currentSpeed = 0.0,
            topSpeed = 0.0,
            averageSpeed = 0.0,
            distance = 0,
            status = STOPPED,
            gpsSignalStrength = 0
        )
    }



    override fun describeContents(): Int {
        return 0
    }


}
