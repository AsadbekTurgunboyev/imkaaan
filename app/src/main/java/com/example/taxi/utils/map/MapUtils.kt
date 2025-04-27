package com.example.taxi.utils.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.taxi.domain.preference.UserPreferenceManager

object MapUtils {
    enum class MapType(val packageName: String) {
        GOOGLE("com.google.android.apps.maps"),
        YANDEX("ru.yandex.yandexnavi"),
        WAZE("com.waze")
    }

    fun findRoute(context: Context, lat: String?, long: String?, preferenceManager: UserPreferenceManager) {
        val a = preferenceManager.getMapSettings()
        val uri = when (a) {
            MapType.GOOGLE.packageName -> Uri.parse("google.navigation:q=${lat},${long}")
            MapType.YANDEX.packageName -> Uri.parse("yandexnavi://build_route_on_map?lat_to=$lat&lon_to=$long")
            MapType.WAZE.packageName -> Uri.parse("waze://?ll=$lat,$long&navigate=yes")
            else -> {
                Uri.parse("yandexnavi://build_route_on_map?lat_to=$lat&lon_to=$long\"")}
        }

        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(a)
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Map application is not installed, redirect to the Google Play Store or notify the user
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${a}")
                setPackage("com.android.vending")
            }
            try {
                context.startActivity(playStoreIntent)
            } catch (ex: ActivityNotFoundException) {
                // Google Play Store is not available, handle this case or notify the user
                Toast.makeText(context, "Iltimos ${a} dasturni o'rnating.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}