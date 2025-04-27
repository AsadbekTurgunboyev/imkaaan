package com.example.taxi.utils

import android.util.Log
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.preference.UserPreferenceManager

object TaxiCalculator {


    fun getCurrentDriveCost(
        dashboardData: DashboardData,
        preferenceManager: UserPreferenceManager,
        secondsElapsed: Long
    ) :String{

        val moneyTotalDistance = preferenceManager.loadMileageData()

        val startCost = preferenceManager.getStartCost()
        val waitTime = ((preferenceManager.getFinishedTimeAcceptOrder() - preferenceManager.getTransitionTime()) / 1000).toInt() + secondsElapsed
        val currentDriveCost: Int = moneyTotalDistance?.let { calculateTotalCost(it,dashboardData.getDistanceText() * 1000).toInt() }?:0
//            plus((waitTime / 60.0) * preferenceManager.getCostWaitTimePerMinute()

        Log.d("narxi", "getCurrentDriveCost: $currentDriveCost")
        return PhoneNumberUtil.formatMoneyNumberPlate(roundToNearestMultiple((currentDriveCost.plus(startCost))).toString())
    }

    private fun calculateTotalCost(mileagePrices: List<MileageData>, remainingDistance: Double): Double {
        var remainingDistanceVar = remainingDistance.toDouble()
        var previousKm = 0.0
        var totalCost = 0.0

        if (mileagePrices.isNotEmpty()) {
            for (mileagePrice in mileagePrices) {
                val currentKm = mileagePrice.value * 1000.0 // value in meters
                val distanceToConsider = minOf(remainingDistanceVar, currentKm - previousKm)
                totalCost += (distanceToConsider / 1000) * mileagePrice.price
                remainingDistanceVar -= distanceToConsider
                previousKm = currentKm
                if (remainingDistanceVar <= 0) {
                    break
                }
            }
            if (remainingDistanceVar > 0) {
                val lastMileagePrice = mileagePrices.last()
                val standardPricePerKm = lastMileagePrice.price
                totalCost += (remainingDistanceVar / 1000) * standardPricePerKm
            }
//            totalCost = kotlin.math.round(totalCost / 500) * 500
        }

        return totalCost
    }

     fun roundToNearestMultiple(number: Int): Int {
         Log.d("BBBB", "roundToNearestMultiple: $number")
         val multiple = 200
        val remainder = number % multiple
        return if (remainder < multiple / 2) {
            number - remainder
        } else {
            number + (multiple - remainder)
        }
    }

}

data class TaxiData(
    var costPerKm: Int? = 0,
    var costWaitTimePerMinute: Int? = 0,
    var costOutCenter: Int? = 0,
    var startCost: Int? = 0,
    var minimalDistance: Int? = 0,
    var minWaitTime: Int? = 0,
    var currentDriveDistance: Int? = 0
)