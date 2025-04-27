package com.example.taxi.domain.drive.drivepath

import android.util.Log
import com.example.taxi.domain.location.LocationPoint
import com.example.taxi.domain.preference.UserPreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.koin.core.context.GlobalContext

class DrivePathBuilder {


    private val drivePath = mutableListOf<DrivePathItemBuilder>()
    private val drivePathLite = mutableListOf<LocationPoint>()


    private val sharedPreferences by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }
    private val gson = Gson()


    init {
        loadPaths()
    }


    private val pathAngleDiffChecker by lazy {
        println("Initializing PathAngleDiffChecker...")
        GlobalContext.get().get<PathAngleDiffChecker>()
    }


    fun addRacePathItem(
        locationPoint: LocationPoint,
        speed: Float,
        time: Long,
        forceAdd: Boolean = false
    ) {
        drivePath.add(
            DrivePathItemBuilder()
                .setLocationPoint(locationPoint)
                .setSpeed(speed)
                .setTime(time)
        )


        if (isCriteriaMatchesForLite(locationPoint) || forceAdd) {
            drivePathLite.add(locationPoint)
        }


        savePaths()
    }


    fun getRacePath(): List<DrivePathItem> {
        return drivePath.map {
            it.build()
        }.toList()
    }


    fun getRacePathLite(): List<LocationPoint> {
        return drivePathLite.toList()
    }


    private fun isCriteriaMatchesForLite(locationPoint: LocationPoint): Boolean {
        return pathAngleDiffChecker.isAngleDiff(locationPoint, true)
    }

    private fun savePaths() {
        val drivePathJson = gson.toJson(drivePath)
        val drivePathLiteJson = gson.toJson(drivePathLite)
        sharedPreferences.savePaths("drivePath", drivePathJson, "drivePathLite", drivePathLiteJson)
    }

    private fun loadPaths() {
        val drivePathJson = sharedPreferences.getPath("drivePath")
        val drivePathLiteJson = sharedPreferences.getPath("drivePathLite")
        if (drivePathJson != "") {
            try {
                val type = object : TypeToken<MutableList<DrivePathItemBuilder>>() {}.type
                drivePath.addAll(gson.fromJson(drivePathJson, type))
            } catch (e: JsonSyntaxException) {
                Log.e("yolak", "loadPaths: ${e.message}")
            }
        }


        if (drivePathLiteJson != "") {
            try {

                val type = object : TypeToken<MutableList<LocationPoint>>() {}.type
                drivePathLite.addAll(gson.fromJson(drivePathLiteJson, type))
            } catch (e: JsonSyntaxException) {

            }
        }
    }
}