package com.example.taxi.ui.home.driver.driveReport

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.taxi.R
import com.example.taxi.databinding.DialogFinishOrderBinding
import com.example.taxi.domain.drive.drivepath.DrivePathItemBuilder
import com.example.taxi.domain.location.LocationPoint
import com.example.taxi.domain.model.BonusResponse
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.finish.FinishCostRequest
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderCompleteRequest
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.driver.DriverViewModel
import com.example.taxi.utils.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.*

class DriveFinishDialog(val raceId: Long, val viewModel: DriveReportViewModel) :
    DialogFragment() {

    private val driverViewModel: DriverViewModel by sharedViewModel()
    private val preferenceManager: UserPreferenceManager by inject()

    var farCenterDistance = 0.0
    var order_history_id = 0
    var distanceMerge:List<Pair<Int, Double>>? = emptyList()

    lateinit var viewBinding: DialogFinishOrderBinding

    var lastLocationPath: List<LocationPoint> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = DialogFinishOrderBinding.inflate(layoutInflater, container, false)

        viewBinding.payWithBonus.visibility =
            if (preferenceManager.getStatusIsTaximeter()) View.GONE else View.VISIBLE
        return viewBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keycode, _ -> keycode == KeyEvent.KEYCODE_BACK }
//
//        dialog.window?.apply {
//            val attributes = attributes
//            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
//            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
//            attributes.gravity = Gravity.BOTTOM
//            setAttributes(attributes)
//        }

        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDrivePath().observe(viewLifecycleOwner) {
            setData(it)
        }
        viewModel.getDriveAnalyticsLiveData().observe(viewLifecycleOwner) {
            renderAnalyticsReportData(it)
        }
        driverViewModel.transferWithBonus.observe(viewLifecycleOwner) {
            transferUi(it)
        }
        
        viewBinding.backShowPriceButton.setOnClickListener {
            backFromBonusUi()
        }

        driverViewModel.confirmationCode.observe(viewLifecycleOwner) {
            confirmBonusUi(it)
        }
        viewBinding.payButton.setOnClickListener {
            val money = viewBinding.editPrice.text.toString().toInt()
            driverViewModel.transferWithBonus(preferenceManager.getOrderId(), money)

        }
        viewBinding.passwordButton.setOnClickListener {
            if (viewBinding.pinView.text?.isEmpty() == true) {
                Toast.makeText(requireContext(), "Tasdiqlash kodini kiriting", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            driverViewModel.confirmBonusPassword(
                order_history_id,
                viewBinding.pinView.text.toString().toInt()
            )
        }
    }

    private fun confirmBonusUi(resource: Resource<MainResponse<Any>>?) {
        resource?.let {
            when (resource.state) {
                ResourceState.ERROR -> {}
                ResourceState.SUCCESS -> {
                    driverViewModel.completedOrder()
                    viewModel.deleteDrive(driveId = raceId)
                    preferenceManager.timeClear()
                    navigateToDashboardFragment()
                }

                ResourceState.LOADING -> {}
            }
        }
    }
    
    

    private fun transferUi(resource: Resource<MainResponse<BonusResponse>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    viewBinding.moneyForBonusLayout.isErrorEnabled = false
                }

                ResourceState.SUCCESS -> {
                    viewBinding.moneyForBonusLayout.isErrorEnabled = false
                    order_history_id = it.data?.data?.order_history_id!!
                    showPasswordUi()
                }

                ResourceState.ERROR -> {
                    viewBinding.moneyForBonusLayout.error = it.message
                }
            }
        }
    }

    private fun showPasswordUi() {
        with(viewBinding) {
            passwordLiner.visibility = View.VISIBLE
            starting.visibility = View.GONE
            bonusLiner.visibility = View.GONE
            passwordButton.visibility = View.VISIBLE
            finishButtonDialog.visibility = View.GONE
            payButton.visibility = View.GONE
        }

    }

    private fun backFromBonusUi() {
        with(viewBinding) {
            backShowPriceButton.visibility = View.GONE
            moneyForBonusTextView.visibility = View.GONE
            titleFinishDialog.text = getString(R.string.yetib_keldingiz)
            payButton.visibility = View.GONE
            finishButtonDialog.visibility = View.VISIBLE
            passwordButton.visibility = View.GONE
            bonusLiner.visibility = View.GONE
            passwordLiner.visibility = View.GONE
            starting.visibility = View.VISIBLE
        }
    }

    private fun showBonusUi() {
        with(viewBinding) {
            backShowPriceButton.visibility = View.VISIBLE
            moneyForBonusTextView.visibility = View.VISIBLE
            finishButtonDialog.visibility = View.GONE
            payButton.visibility = View.VISIBLE
            passwordButton.visibility = View.GONE
            bonusLiner.visibility = View.VISIBLE
            titleFinishDialog.text = getString(R.string.bonus_orqali_to_lov)
            starting.visibility = View.GONE
        }
    }

    private fun totalPathDistance(points: List<LocationPoint>): Double {
        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += haversineDistance(points[i], points[i + 1])
        }

        return totalDistance
    }


    private fun loadPaths(): List<LocationPoint> {
        val drivePathLite = mutableListOf<LocationPoint>()
        val gson = Gson()
        val drivePathLiteJson = preferenceManager.getPath("drivePathLite")

        if (drivePathLiteJson != "") {
            try {

                val type = object : TypeToken<MutableList<LocationPoint>>() {}.type
                drivePathLite.addAll(gson.fromJson(drivePathLiteJson, type))
            } catch (e: JsonSyntaxException) {

            }
        }

        return drivePathLite
    }

    private fun setData(locationPoints: List<LocationPoint>?) {

        lastLocationPath = locationPoints?: loadPaths()
        val centralPoint = preferenceManager.getCentralLocationPoint()
        val minDistanceInMeters = preferenceManager.getCenterRadius()?.toInt() ?: 0

        if (centralPoint != null) {
            distanceMerge = calculateDistancesDetailed(
                lastLocationPath,
                centralPoint.latitude, centralPoint.longitude, minDistanceInMeters
            )
        }

        if (centralPoint != null) {
            val farPoints =
                pointsWithinDistance(lastLocationPath, centralPoint, minDistanceInMeters)
            farCenterDistance = totalPathDistance(farPoints)
        }
    }

    private fun mergeSegments(segments: List<Pair<Int, Double>>): List<Pair<Int, Double>> {
        if (segments.isEmpty()) return emptyList()

        val mergedSegments = mutableListOf<Pair<Int, Double>>()
        var currentStatus = segments[0].first
        var currentDistance = 0.0

        for (segment in segments) {
            if (segment.first == currentStatus) {
                currentDistance += segment.second
            } else {
                mergedSegments.add(Pair(currentStatus, currentDistance))
                currentStatus = segment.first
                currentDistance = segment.second
            }
        }
        mergedSegments.add(Pair(currentStatus, currentDistance))

        return mergedSegments
    }

    private fun calculateDistancesDetailed(
        locationPoints: List<LocationPoint>,
        centerLat: Double,
        centerLon: Double,
        radius: Int
    ): List<Pair<Int, Double>> {
        val segments = mutableListOf<Pair<Int, Double>>()
        if (locationPoints.size < 2) return emptyList()

        for (i in 0 until locationPoints.size - 1) {
            val point1 = locationPoints[i]
            val point2 = locationPoints[i + 1]

            val distance = haversineDistance(point1, point2)
            val distanceFromCenter1 = haversineDistance(LocationPoint(centerLat, centerLon), point1)
            val distanceFromCenter2 =
                haversineDistance(LocationPoint(centerLat, centerLon), point2)

            val locationStatus =
                if (distanceFromCenter1 <= radius && distanceFromCenter2 <= radius) {
                    0 // shahar ichida harakatlangan
                } else {
                    1 // shahar tashqarisida harakatlangan
                }
            segments.add(Pair(locationStatus, distance / 1000)) // km ga o'tkazamiz
        }

        return mergeSegments(segments)
    }


    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme // Optional: Customize the theme of the bottom sheet dialog
    }

    private fun calculateTotalPrice(pairList: List<Pair<Int, Double>>, mileageData: List<MileageData>): Double {
        var totalPrice = 0.0
        var currentDistance = 0.0

        val firstPrice: Int = mileageData[0].price
        pairList.forEach { pair ->
            val (locationType, distance) = pair
            var remainingDistance = distance

            while (remainingDistance > 0) {
                val nextDistance = currentDistance.toInt() + 1
                val mileage = mileageData.find { it.value == nextDistance }
                val mileageToUse = mileage ?: mileageData.last()

                val distanceToNextKm = nextDistance - currentDistance
                val travelDistance = minOf(remainingDistance, distanceToNextKm)

                when (locationType) {
                    0 -> totalPrice += mileageToUse.price * travelDistance
                    1 -> totalPrice += mileageToUse.price_out_center * travelDistance
                }

                currentDistance += travelDistance
                remainingDistance -= travelDistance
            }
        }


        if (currentDistance<1){
            return (firstPrice * 1).toDouble()
        }else{
            return totalPrice
        }
    }

    private fun renderAnalyticsReportData(driveAnalyticsData: DriveAnalyticsData) {


        val costPerKm: Int = preferenceManager.getCostPerKm() // Suppose it returns Int
        val totalDistance: Double =
            driveAnalyticsData.getTotalDistanceAsDouble() // Suppose it returns Double
        val minDistance = driveAnalyticsData.getMinDistanceAsKm(preferenceManager.getMinDistance())

        val isDistance = totalDistance - (farCenterDistance / 1000.0)
        var totalInCenterDistance = 0.0
        if (isDistance > minDistance) {
            totalInCenterDistance = isDistance - (minDistance)
        }

        if (isDistance < 0){
            farCenterDistance = totalDistance
        }else{
            farCenterDistance /= 1000.toDouble()
        }

        val moneyForInDistance = (totalInCenterDistance) * costPerKm
        val moneyForFarDistance =
            (farCenterDistance) * preferenceManager.getCostOutCenter()

        val moneyTotalDistance = preferenceManager.loadMileageData()
            ?.let { distanceMerge?.let { it1 -> calculateTotalPrice(it1,it).roundToInt() } }
            ?.let { TaxiCalculator.roundToNearestMultiple(it) }

        val moneySecondVersion =
            preferenceManager.loadMileageData()?.let { calculateTotalCost(it,driveAnalyticsData.getTotalDistanceAsDouble() * 1000) }?.toInt()
                ?.let {
                    TaxiCalculator.roundToNearestMultiple(
                        it
                    )
                }

        Log.d("BBBB", "renderAnalyticsReportData: $moneySecondVersion")

        val moneyWithKm: Int = TaxiCalculator.roundToNearestMultiple((moneyForInDistance + moneyForFarDistance).roundToInt())// This will also work
//        val moneyWithKm: Int =
//            TaxiCalculator.roundToNearestMultiple(moneyTotalDistance.roundToInt())// This will also work

        val waitTime = driveAnalyticsData.getPauseTimeWithSecond() + ConversionUtil.getAllWaitTime()
        val moneyWithTime =
            TaxiCalculator.roundToNearestMultiple(((waitTime / 60.toDouble()) * preferenceManager.getCostWaitTimePerMinute()).toInt())


        preferenceManager.clearPassengerPhone()

//        val allPrice =
//            if (preferenceManager.isHasDestinationSecond()) {
//                moneyWithTime + preferenceManager.getStartCost()
//            } else {
//                moneyWithTime + moneyWithKm + preferenceManager.getStartCost()
//            }
        val allPrice =
            if (preferenceManager.isHasDestinationSecond()) {
                moneyWithTime + preferenceManager.getStartCost()
            } else {
                if (moneyTotalDistance != null && moneyTotalDistance > 0) {
                    moneyWithTime + moneyTotalDistance + preferenceManager.getStartCost()
                } else {
                    if (moneySecondVersion != null && moneySecondVersion > 0){
                        moneyWithTime + preferenceManager.getStartCost() + moneySecondVersion
                    }else{
                        moneyWithTime + preferenceManager.getStartCost()
                    }
                }
            }

        viewBinding.waitTimeTxt.text = ConversionUtil.convertSecondsToMinutes(waitTime.toInt())
        viewBinding.km.text = driveAnalyticsData.getTotalDistanceAsString()
        viewBinding.startcost.text =
            PhoneNumberUtil.formatMoneyNumberPlate(preferenceManager.getStartCost().toString())

        viewBinding.priceTripNoWait.text = if (moneyTotalDistance != null){
            PhoneNumberUtil.formatMoneyNumberPlate(TaxiCalculator.roundToNearestMultiple(moneyTotalDistance).toString())
        }else{
            moneySecondVersion?.let {  PhoneNumberUtil.formatMoneyNumberPlate(TaxiCalculator.roundToNearestMultiple(it).toString()) }
        }
//            moneyTotalDistance?.let { TaxiCalculator.roundToNearestMultiple(it).toString() }?.let {
//                PhoneNumberUtil.formatMoneyNumberPlate(
//                    it
//                )
//            }

        val order =
            OrderCompleteRequest(
                cost = allPrice,
                distance = driveAnalyticsData.getDistance(),
                wait_time = waitTime.toInt(),
                wait_cost = moneyWithTime,
                coordinates = lastLocationPath
            )

        preferenceManager.saveLastRace(order, 1)

        viewBinding.payWithBonus.setOnClickListener {
            preferenceManager.clearPath()
            driverViewModel.completeOrderForBonus(
                order
            )
            showBonusUi()
        }


        viewBinding.priceWait.text =
            PhoneNumberUtil.formatMoneyNumberPlate(moneyWithTime.toString())

        viewBinding.priceAll.text = PhoneNumberUtil.formatMoneyNumberPlate(allPrice.toString())
        viewBinding.moneyForBonusTextView.text =
            PhoneNumberUtil.formatMoneyNumberPlate(allPrice.toString())
        viewBinding.editPrice.setText(allPrice.toString())
        viewBinding.finishButtonDialog.setOnClickListener {
            preferenceManager.clearPath()
            driverViewModel.completeOrder(
                order
            )
        }

        driverViewModel.completeOrder.observe(viewLifecycleOwner) {
            when (it.state) {
                ResourceState.ERROR -> {
                    preferenceManager.saveLastRace(order, 1)
                    viewModel.deleteDrive(driveId = raceId)
                    preferenceManager.timeClear()
                    viewBinding.finishButtonDialog.stopAnimation()
                    navigateToDashboardFragment()
                }

                ResourceState.SUCCESS -> {
                    preferenceManager.saveLastRace(order, -1)
                    viewBinding.finishButtonDialog.stopAnimation()
                    driverViewModel.completedOrder()
                    viewModel.deleteDrive(driveId = raceId)
                    preferenceManager.timeClear()
                    navigateToDashboardFragment()
                }

                ResourceState.LOADING -> {
                    viewBinding.finishButtonDialog.startAnimation()

                }
            }

        }

    }

    private fun calculateTotalCost(mileagePrices: List<MileageData>, remainingDistance: Double): Double {
        var remainingDistanceVar = remainingDistance.toDouble()
        var previousKm = 0.0
        var totalCost = 0.0
        Log.d("FFFF", "calculateTotalCost: $mileagePrices")
        Log.d("FFFF", "calculateTotalCost: $remainingDistance")
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


    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.clearPath()
        viewBinding.finishButtonDialog.dispose()
    }

    private fun navigateToDashboardFragment() {
        preferenceManager.clearPath()
        activity?.let { currentActivity ->
            val intent = currentActivity.intent
            currentActivity.finish()
            currentActivity.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            dismiss()
            startActivity(intent)
            currentActivity.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private fun pointsWithinDistance(
        points: List<LocationPoint>,
        center: LocationPoint,
        minDistance: Int
    ): List<LocationPoint> {
        return points.filter { haversineDistance(it, center) >= minDistance }
    }

    private fun haversineDistance(point1: LocationPoint, point2: LocationPoint): Double {
        val R = 6371e3  // radius of Earth in meters
        val lat1 = point1.latitude * PI / 180  // convert degrees to radians
        val lat2 = point2.latitude * PI / 180
        val deltaLat = (point2.latitude - point1.latitude) * PI / 180
        val deltaLon = (point2.longitude - point1.longitude) * PI / 180

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    companion object {
        val TAG = "finishDialog"
    }


    /**
     * Xatolik keyinchalik bo'lib qolsa shu funksiya shahar ichi va tashqarisini hisoblaydi
     */

//    fun calculateDistances(locationPoints: List<LocationPoint>, centerLat: Double, centerLon: Double, radius: Double): Pair<Double, Double> {
//        var insideDistance = 0.0
//        var outsideDistance = 0.0
//
//        for (i in 0 until locationPoints.size - 1) {
//            val point1 = locationPoints[i]
//            val point2 = locationPoints[i + 1]
//
//            val distance = haversine(point1.latitude, point1.longitude, point2.latitude, point2.longitude)
//            val distanceFromCenter1 = haversine(centerLat, centerLon, point1.latitude, point1.longitude)
//            val distanceFromCenter2 = haversine(centerLat, centerLon, point2.latitude, point2.longitude)
//
//            if (distanceFromCenter1 <= radius && distanceFromCenter2 <= radius) {
//                insideDistance += distance
//            } else {
//                outsideDistance += distance
//            }
//        }
//
//        return Pair(insideDistance, outsideDistance)
//    }
}