package com.example.taxi.utils

import android.content.Context
import com.example.taxi.R
import com.example.taxi.domain.model.map.MapLocation
import com.example.taxi.domain.model.settings.SettingsModel

object ConstantsUtils {

    const val REQUEST_CODE_STT_START = 11
    const val REQUEST_CODE_STT_END = 12
    const val LOCATION_PERMISSIONS_REQUEST_CODE = 34


    const val NAVIGATION = 1
    const val LANGUAGE = 2
    const val THEME = 3
    var locationStart: MapLocation = MapLocation(0.0, 0.0)
    var locationDestination: MapLocation = MapLocation(0.0, 0.0)
    var locationDestination2: MapLocation? = MapLocation(0.0, 0.0)

    val mapOptions = listOf(
        SettingsModel("Google Maps", "com.google.android.apps.maps"),
        SettingsModel("Yandex Navigator", "ru.yandex.yandexnavi"),
        SettingsModel("Waze", "com.waze")
    )

    fun getThemeOptions(context: Context): List<SettingsModel> {
        return listOf(
            SettingsModel(context.getString(R.string.auto), "auto"),
            SettingsModel(context.getString(R.string.day), "light"),
            SettingsModel(context.getString(R.string.night), "dark")
        )
    }


    const val STATUS_GOING_TO_CLIENT = 1;
    const val STATUS_WAITING_FOR_CLIENT = 2;
    const val STATUS_ON_THE_WAY = 3;
    const val STATUS_CANCELLED = 4;
    const val STATUS_FINISHED = 5;
    const val DRIVER_IS_ONLINE = 1
    const val DRIVER_IS_OFFLINE = 2

}