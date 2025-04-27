package com.example.taxi.domain.model.settings

import com.example.taxi.domain.model.MainResponse

data class SettingsData(
    val name: String,
    val info: String,
    val value: String
)


object DataNames {
    const val CENTER_LATITUDE = "center_latitude"
    const val CENTER_LONGITUDE = "center_longitude"
    const val CENTER_RADIUS = "center_radius"
    const val NIGHT_MODE_START = "night_mode_start"
    const val NIGHT_MODE_END = "night_mode_end"
    const val MIN_RUNNING_APP_VERSION = "min_running_app_version"
    const val PHONE_NUMBER = "phone_number"
}

fun MainResponse<List<SettingsData>>.getItemValueByName(name: String): String? {
    val item = data.find { it.name == name }
    return item?.value
}