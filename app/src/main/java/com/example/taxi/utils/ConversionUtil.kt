package com.example.taxi.utils

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.taxi.domain.preference.UserPreferenceManager
import org.koin.core.context.GlobalContext
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt


private const val KM_MULT = 3.6
private const val MILES_MULT = 2.23694
private const val SECONDS_HOUR_MULT = 0.000277778
private const val METRE_KM = 0.001
private const val METRE_MILES = 0.000621371

object ConversionUtil {

    private val userPreferenceManager by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val r = 6371 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = StrictMath.sin(dLat / 2) * StrictMath.sin(dLat / 2) +
                StrictMath.cos(Math.toRadians(lat1)) * StrictMath.cos(Math.toRadians(lat2)) *
                StrictMath.sin(dLon / 2) * StrictMath.sin(dLon / 2)

        val c = 2 * StrictMath.atan2(StrictMath.sqrt(a), StrictMath.sqrt(1 - a))

        val distance = r * c

        return approxRoundedOffToKm(distance * 1.1)
    }

    fun calculateDistanceDouble(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = StrictMath.sin(dLat / 2) * StrictMath.sin(dLat / 2) +
                StrictMath.cos(Math.toRadians(lat1)) * StrictMath.cos(Math.toRadians(lat2)) *
                StrictMath.sin(dLon / 2) * StrictMath.sin(dLon / 2)

        val c = 2 * StrictMath.atan2(StrictMath.sqrt(a), StrictMath.sqrt(1 - a))

        val distance = r * c

        return distance * 1.1
    }



    fun convertToCyrillic(input: String): String {
        if (userPreferenceManager.getLanguage() == UserPreferenceManager.Language.UZBEK ) {
            return input
        }

        val latinToCyrillicMap = listOf(
            "oʻ" to "ў", "g‘" to "ғ", "sh" to "ш", "ch" to "ч", "ng" to "нг",
            "ya" to "я", "yo" to "ё", "g'" to "ғ", "o'" to "ў",
            "a" to "а", "b" to "б", "c" to "с", "d" to "д", "e" to "е", "f" to "ф",
            "g" to "г", "h" to "ҳ", "i" to "и", "j" to "ж", "k" to "к", "l" to "л",
            "m" to "м", "n" to "н", "o" to "о", "p" to "п", "q" to "қ", "r" to "р",
            "s" to "с", "t" to "т", "u" to "у", "v" to "в", "x" to "х", "y" to "й",
            "z" to "з"
        ).sortedByDescending { it.first.length }

        val result = StringBuilder()
        var i = 0

        while (i < input.length) {
            var found = false
            for ((latin, cyrillic) in latinToCyrillicMap) {
                val endIdx = i + latin.length
                if (endIdx <= input.length && input.substring(i, endIdx).equals(latin, ignoreCase = true)) {
                    var matchedCyrillic = cyrillic
                    if (i == 0 && latin.equals("e", ignoreCase = true)) {
                        matchedCyrillic = "э"
                    }
                    val matchedSubstring = input.substring(i, endIdx)
                    val converted = if (matchedSubstring[0].isUpperCase()) matchedCyrillic.replaceFirstChar { it.uppercase() } else matchedCyrillic
                    result.append(converted)
                    i += latin.length
                    found = true
                    break
                }
            }
            if (!found) {
                result.append(input[i])
                i++
            }
        }

        return result.toString()
    }




    fun calculateSeconds():Int{
        val timestampMilliseconds: Long = System.currentTimeMillis()
        val a = timestampMilliseconds - userPreferenceManager.getStartedTimeAcceptOrder()
        val seconds = (a / 1000).toInt()
        return if (seconds > 0) seconds else 0
    }

    fun getWaitTime(): Int{
        val timestampMilliseconds: Long = System.currentTimeMillis()
        val a = timestampMilliseconds - userPreferenceManager.getStartedTimeAcceptOrder()

        val seconds = (a / 1000).toInt()

        return if (seconds > userPreferenceManager.getMinWaitTime()) seconds else 0
    }

    fun getAllWaitTime(): Int{
        val startTime = userPreferenceManager.getStartedTimeAcceptOrder()
        val endTime = userPreferenceManager.getFinishedTimeAcceptOrder()
        val seconds = (endTime - startTime) / 1000
        val waitTime = seconds.toInt() - userPreferenceManager.getMinWaitTime()
        return if (waitTime > 0) waitTime else 0
    }
    private fun approxRoundedOffToKm(value: Double): String {
        val decimalFormat = DecimalFormat("#.#")
        decimalFormat.maximumFractionDigits = 1
        val roundedValue = decimalFormat.format(value)

        return "~$roundedValue km"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val decimalFormatter = MeasureFormat.getInstance(
        Locale.getDefault(),
        MeasureFormat.FormatWidth.SHORT,
        android.icu.text.DecimalFormat("#.#")
    )
    @RequiresApi(Build.VERSION_CODES.N)
    private val normalFormatter = MeasureFormat.getInstance(
        Locale.getDefault(),
        MeasureFormat.FormatWidth.SHORT,
        android.icu.text.DecimalFormat("#")
    )

    private val isImperial
        get() = userPreferenceManager.getDistanceUnit() == UserPreferenceManager.DistanceUnit.MILES

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistanceUnit(): String = if (isImperial) {
        decimalFormatter.format(Measure(0, MeasureUnit.MILE)).replace("0", "").trim()
    } else {
        decimalFormatter.format(Measure(0, MeasureUnit.KILOMETER)).replace("0", "").trim()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getSpeedUnit(): String = if (isImperial) {
        normalFormatter.format(Measure(0, MeasureUnit.MILE_PER_HOUR)).replace("0", "").trim()
    } else {
        normalFormatter.format(Measure(0, MeasureUnit.KILOMETER_PER_HOUR)).replace("0", "").trim()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistance(metre: Double): Double = if (isImperial) {
        convertToMiles(metre)
    } else {
        convertToKm(metre)
    }

    fun roundTo2Decimals(number: Double): Double {
        val factor = 100.0
        return (number * factor).roundToInt() / factor
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistanceWithKm(metre: Double): String = "${roundTo2Decimals(getDistance(metre))}"

    fun getSpeed(metrePerSecond: Double): Double = if (isImperial) {
        convertToMilesPerHr(metrePerSecond).toInt().toDouble()
    } else {
        convertToKmPerHr(metrePerSecond).toInt().toDouble()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getDistanceWithUnit(metre: Double): String = if (isImperial) {
        decimalFormatter.format(Measure(convertToMiles(metre), MeasureUnit.MILE))
    } else {
        decimalFormatter.format(Measure(convertToKm(metre), MeasureUnit.KILOMETER))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getSpeedWithUnit(metrePerSecond: Double): String = if (isImperial) {
        normalFormatter.format(
            Measure(
                convertToMilesPerHr(metrePerSecond),
                MeasureUnit.MILE_PER_HOUR
            )
        )
    } else {
        normalFormatter.format(
            Measure(
                convertToKmPerHr(metrePerSecond),
                MeasureUnit.KILOMETER_PER_HOUR
            )
        )
    }

     fun convertSecondsToMinutes(seconds: Int): String {

        val minutes = seconds / 60
        val secondsRemainder = seconds % 60
        val formattedMinutes = String.format("%02d", minutes)
        val formattedSeconds = String.format("%02d", secondsRemainder)
        return "$formattedMinutes:$formattedSeconds"
    }
    fun getSpeedStr(
        metrePerSecond: Double
    ): String {
        val speed: Double = getSpeed(metrePerSecond)
        return speed.toInt().toString()
    }

    fun convertToHour(sec: Double): Double {
        return if (sec > 0.2) {
            sec * SECONDS_HOUR_MULT
        } else 0.0
    }

     fun convertToKm(metre: Double): Double {
        return if (metre > 10) {
            metre * METRE_KM
        } else 0.0
    }

    private fun convertToMiles(metre: Double): Double {
        return if (metre > 20) {
            metre * METRE_MILES
        } else 0.0
    }

    private fun convertToKmPerHr(metrePerSec: Double): Double {
        return if (metrePerSec > 0.3) {
            metrePerSec * KM_MULT
        } else 0.0
    }

    fun convertToMetrePerSec(kmPerHour: Double): Double {
        return kmPerHour / KM_MULT
    }

    private fun convertToMilesPerHr(metrePerSec: Double): Double {
        return if (metrePerSec > 0.5) {
            metrePerSec * MILES_MULT
        } else 0.0
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun roundDouble(value: Double) = android.icu.text.DecimalFormat("#.#").format(value)

}