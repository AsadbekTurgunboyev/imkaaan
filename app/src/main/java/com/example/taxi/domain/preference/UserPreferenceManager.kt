package com.example.taxi.domain.preference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.taxi.domain.location.LocationPoint
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.OrderCompleteRequest
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.model.register.confirm_password.UserData
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.utils.isMetric
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.util.Locale

class UserPreferenceManager(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val PHONE_NUMBER_KEY = "phone_number"
        const val COST_PER_KM = "cost_per_km"
        const val MIN_DISTANCE = "min_distance"
        const val START_COST = "start_cost"
        const val MIN_WAIT_TIME = "min_wait_time"
        const val IS_SEEN_STATUS = "seen_status"
        const val IS_SEEN_INTRO = "seen_intro"
        const val PASSENGER_PHONE = "passenger_phone"
        const val PASSENGER_NAME = "passenger_name"
        const val PASSENGER_COMMENT = "passenger_comment"
        const val DESTINATION1 = "destination_1"
        const val DESTINATION1_LONG = "destination_1_long"
        const val DESTINATION2_LONG = "destination_2_long"
        const val DESTINATION1_LAT = "destination_1_lat"
        const val DESTINATION2_LAT = "destination_2_lat"
        const val DESTINATION2 = "destination_2"
        const val DRIVER_STATUS = "driver_status"
        const val CENTER_RADIUS = "center_radius"
        const val COST_OUT_CENTER = "cost_out_center"
        const val COST_WAIT_TIME_PER_MINUTE = "cost_wait_time_per_minute"
        const val LAT = "center_lat"
        const val LONG = "center_long"
        const val LAST_RACE_ID = "last_race_id"
        const val LAST_RACE_DISTANCE = "last_race_distance"
        const val LAST_RACE_COST = "last_race_cost"
        const val LAST_RACE_WAIT_COST = "last_race_wait_cost"
        const val LAST_RACE_WAIT_TIME = "last_race_wait_time"
        const val KEY_TOGGLE_STATE = "ToggleState"
        const val IS_TAXIMETER = "is_taximeter"
        const val ORDER_ID = "order_id"
        const val ADVERTISING = "is_advertising"

    }

    fun saveStartCostUpdate(start_cost: Int) {
        prefs.edit().putInt(START_COST, start_cost).apply()
    }

    fun saveAdvertisingStatus(status: Int) {
        prefs.edit().putInt(ADVERTISING, status).apply()
    }

    fun getAdvertisingStatus(): Int {
        return prefs.getInt(ADVERTISING, -1)
    }

    fun savePriceSettings(order: OrderAccept<UserModel,MileageData>) {

        with(prefs.edit()) {
//
            if (order.getCostPerKm() == 0) putInt(COST_PER_KM, 2000) else putInt(
                COST_PER_KM, order.getCostPerKm()
            )

            putInt(MIN_DISTANCE, order.getMinDistance())
            if (order.start_cost != 0) putInt(START_COST, order.start_cost) else putInt(
                START_COST, order.getMinCost()
            )
//            putInt(START_COST, 6000)

            putInt(COST_OUT_CENTER, order.getCostPerKmOutside())
            putInt(MIN_WAIT_TIME, order.getMinWaitTime())
            putString(PASSENGER_PHONE, order.user.phone)
            putString(PASSENGER_COMMENT, order.comment)
            putString(PASSENGER_NAME, order.user.name)
            putString(DESTINATION1, order.address.from)
            putString(DESTINATION1_LONG, order.longitude1)
            putString(DESTINATION2_LONG, order.longitude2)
            putString(DESTINATION1_LAT, order.latitude1)
            putString(DESTINATION2_LAT, order.latitude2)
            putInt(ORDER_ID, order.id)
            putString("mileage_data", order.mileage_prices?.let { convertToJsonMileageData(it) })
//
            if (order.getCostMinWaitTimePerMinute() == 0) putInt(
                COST_WAIT_TIME_PER_MINUTE, 500
            ) else putInt(COST_WAIT_TIME_PER_MINUTE, order.getCostMinWaitTimePerMinute())

            putString(DESTINATION2, order.address.to)
        }.apply()
    }



    fun getCurrentDriveOrderId(): Int{
        return prefs.getInt("_orderId",-1)
    }



    fun saveStatusIsTaximeter(isOn: Boolean) {
        prefs.edit().putBoolean(IS_TAXIMETER, isOn).apply()
    }

    fun clearOrderId(){
        prefs.edit().putInt(ORDER_ID,0).apply()
    }
    fun getOrderId() = prefs.getInt(ORDER_ID, 0)

    fun getStatusIsTaximeter(): Boolean = prefs.getBoolean(IS_TAXIMETER, false)

    fun saveToggleState(isOn: Boolean) {
        Log.d("jarayon", "saveToggleState: $isOn")
        prefs.edit().putBoolean(KEY_TOGGLE_STATE, isOn).apply()
    }

    fun getToggleState(): Boolean {
        return prefs.getBoolean(KEY_TOGGLE_STATE, false)
    }

    fun saveStateTime(elapsedTime: Int, isCountingDown: Boolean) {
        prefs.edit().apply {
            putInt("elapsedTime", elapsedTime)
            putBoolean("isCountingDown", isCountingDown)
            apply()
        }
    }

    fun saveIsCountingDown(isCountingDown: Boolean) {
        prefs.edit().putBoolean("isCountingDown", isCountingDown).apply()
    }

    fun getElapsedTime(): Int = prefs.getInt("elapsedTime", 0)

    fun getIsCountingDown(): Boolean = prefs.getBoolean("isCountingDown", true)

    fun setStartedTimeAcceptOrder(time: Long) {
        prefs.edit().putLong("startOrderTime", time).apply()
    }

    fun setFinishedTimeOrder(time: Long) {
        prefs.edit().putLong("finishOrderTime", time).apply()
    }

    fun getStartedTimeAcceptOrder(): Long {
        return prefs.getLong("startOrderTime", System.currentTimeMillis())
    }

    fun getFinishedTimeAcceptOrder(): Long = prefs.getLong("finishOrderTime", 0)

    fun getCostWaitTimePerMinute(): Int = prefs.getInt(COST_WAIT_TIME_PER_MINUTE, 0)

    fun getPassengerPhone(): String? = prefs.getString(PASSENGER_PHONE, null)

    fun getDestination2Address(): String? = prefs.getString(DESTINATION2, "")

    fun getDestinationAddress(): String? = prefs.getString(DESTINATION1, "")

    fun getPassengerName(): String? = prefs.getString(PASSENGER_NAME, "")

    fun getPassengerComment(): String? = prefs.getString(PASSENGER_COMMENT, "-")

    fun getDestination1Long(): String? = prefs.getString(DESTINATION1_LONG, "0.0")
    fun getDestination2Long(): String? = prefs.getString(DESTINATION2_LONG, "0.0")

    fun getDestination1Lat(): String? = prefs.getString(DESTINATION1_LAT, "0.0")
    fun getDestination2Lat(): String? = prefs.getString(DESTINATION2_LAT, "0.0")

    fun isHasDestinationSecond(): Boolean =
        getDestination2Long() != "0.0" && getDestination2Lat() != "0.0"

    fun clearPassengerPhone() {
        prefs.edit().remove(PASSENGER_PHONE).apply()
    }

    fun getCostPerKm(): Int = prefs.getInt(COST_PER_KM, 0)

    fun getCostOutCenter(): Int = prefs.getInt(COST_OUT_CENTER, 0)

    fun getMinDistance(): Int = prefs.getInt(MIN_DISTANCE, 0)

    fun getSharedPreferences(): SharedPreferences {
        return prefs
    }

    fun getStartCost(): Int = prefs.getInt(START_COST, 0)

    fun getMinWaitTime(): Int = prefs.getInt(MIN_WAIT_TIME, 0)

    fun getIsSeenStatus(): Boolean = prefs.getBoolean(IS_SEEN_STATUS, false)

    fun getPrivacyPolicyReadStatus(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("privacy_read_status", false)
    }

    fun setRegisterComplete() {
        prefs.edit().putBoolean("is_completed", true).apply()
    }

    fun getRegisterComplete(): Boolean {
        return prefs.getBoolean("is_completed", false)
    }

    fun setPrivacyPolicyReadStatus() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean("privacy_read_status", true).apply()
    }

    /**
    this save call center phone
     */
    fun saveCallPhoneNumber(phoneNumber: String) {
        prefs.edit().putString(PHONE_NUMBER_KEY, phoneNumber).apply()
    }

    fun getCenterRadius() = prefs.getString(CENTER_RADIUS, null)

    fun getCentralLat() = prefs.getString(LAT, null)

    fun getCentralLong() = prefs.getString(LONG, null)

    /**
     * this function get call center phone
     */
    fun getPhoneNumber(): String? {
        return prefs.getString(PHONE_NUMBER_KEY, null)
    }

    init {
        loadDefault()
    }

    fun getCentralLocationPoint(): LocationPoint? {
        val lat = getCentralLat()?.toDouble()
        val long = getCentralLong()?.toDouble()
        return lat?.let { long?.let { it1 -> LocationPoint(it, it1) } }
    }

    private fun loadDefault() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        when (getDistanceUnit()) {
            DistanceUnit.KILOMETERS -> sharedPreferences.edit().putString("distance_unit", "km")
                .apply()

            DistanceUnit.MILES -> sharedPreferences.edit().putString("distance_unit", "miles")
                .apply()
        }
    }

    fun loadMileageData(): List<MileageData>? {
        val gson = Gson()
        val jsonString = prefs.getString("mileage_data", null)
        val type = object : TypeToken<List<MileageData>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private fun convertToJsonMileageData(mileageDataList: List<MileageData>): String {
        val gson = Gson()
        return gson.toJson(mileageDataList)
    }

    fun getTheme(): ThemeStyle {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return when (sharedPreferences.getString("theme", "auto")) {
            "light" -> {
                ThemeStyle.LIGHT
            }

            "dark" -> {
                ThemeStyle.DARK
            }

            else -> {
                ThemeStyle.AUTO
            }
        }
    }

    fun setTheme(value: String) {
        prefs.edit().putString("theme", value).apply()
    }

    fun getThemeOptions() = prefs.getString("theme", "auto")

    fun getLanguage(): Language {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return when (sharedPreferences.getString("language", "la")) {
            "la" -> {
                Language.UZBEK
            }

            "ru" -> {
                Language.RUSSIAN
            }

            else -> {
                Language.KRILL
            }
        }
    }


    fun saveToken(token: String) {
        prefs.edit().putString("_token", token).apply()
    }

    fun savePhone(phone: String) {
        prefs.edit().putString("phone", phone).apply()
    }

    fun getDriverName(): String? = prefs.getString("firstName", "")
    fun getDriverPhone(): String? = prefs.getString("phone", "")

    fun saveDriverAllData(data: SelfieAllData<IsCompletedModel, StatusModel>) {
        with(prefs.edit()) {
            putString("firstName", data.first_name)
            putString("phone", data.phone)
            putInt("id", data.id)
            putString("status", data.status.string)
            putInt("statusInt", data.status.int)
            putString("photo", data.photo)

        }.apply()
    }

    fun getDriverID(): Int = prefs.getInt("id", -1)


    fun saveResponse(registerModel: UserData<IsCompletedModel>) {
        with(prefs.edit()) {
            putString("phone", registerModel.phone)
            putString("token", registerModel.authKey)
            putString("name", registerModel.firstName)

        }.apply()
        val complete = registerModel.isCompleted.string
        if (complete) {
            setRegisterComplete()
        }
    }

    fun getTokenForPhone(): String? {
        return prefs.getString("_token", null)
    }

    fun saveSeenStatus(isSeen: Boolean) {
        prefs.edit().putBoolean(IS_SEEN_STATUS, isSeen).apply()
    }

    fun saveSeenIntro(isSeen: Boolean) {
        prefs.edit().putBoolean(IS_SEEN_INTRO, isSeen).apply()
    }

    fun getIsSeenIntro(): Boolean = prefs.getBoolean(IS_SEEN_INTRO, false)

    fun getPhone(): String? {
        return prefs.getString("phone", null)
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getTypeNumber(): Int? {
        val jsonString = prefs.getString("data", null)
        return if (jsonString != null) JSONObject(jsonString).getJSONObject("type")
            .getInt("number") else null
    }

    fun getTypeName(): String? {
        val jsonString = prefs.getString("data", null)
        return if (jsonString != null) JSONObject(jsonString).getJSONObject("type")
            .getString("name") else null
    }

    fun clear() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }

    enum class DistanceUnit {
        KILOMETERS, MILES
    }


    enum class ThemeStyle {
        AUTO, LIGHT, DARK
    }

    enum class Language(val code: String) {
        RUSSIAN("ru"), UZBEK("la"), KRILL("uz")
    }

    fun setDriverStatus(status: DriverStatus) {
        prefs.edit().putString(DRIVER_STATUS, status.name).apply()
    }

    fun getDriverStatus(): DriverStatus {
        return DriverStatus.valueOf(prefs.getString(DRIVER_STATUS, DriverStatus.COMPLETED.name)!!)
    }

//    fun setAndGetDriverStatus(status: DriverStatus = DriverStatus.COMPLETED): DriverStatus {
//        prefs.edit().putString(DRIVER_STATUS, status.name).apply()
//        Log.d("tekshirish", "setAndGetDriverStatus: ${status.name}")
//        return DriverStatus.valueOf(prefs.getString(DRIVER_STATUS, DriverStatus.COMPLETED.name)!!)
//    }

    enum class DriverStatus {
        ACCEPTED, ARRIVED, STARTED, COMPLETED
    }


    fun getDistanceUnit(): DistanceUnit {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val distanceUnit = sharedPreferences.getString("distance_unit", "km")
        return when {
            distanceUnit == "km" -> {
                DistanceUnit.KILOMETERS
            }

            distanceUnit == "miles" -> {
                DistanceUnit.MILES
            }

            Locale.getDefault().isMetric() -> {
                DistanceUnit.KILOMETERS
            }

            else -> {
                DistanceUnit.KILOMETERS
            }
        }
    }

    fun setLanguage(language: Language) {
        prefs.edit().putString("language", language.code).apply()
    }

    fun setSettings(centerLat: String?, centerLong: String?, centerRadius: String?) {
        val editor = prefs.edit()
        centerLat?.let { editor.putString(LAT, it) }
        centerLong?.let { editor.putString(LONG, it) }
        centerRadius?.let { editor.putString(CENTER_RADIUS, it) }
        editor.apply()
    }

    fun setIsOrderCancel(b: Boolean) {
        prefs.edit().putBoolean("isOrderCancel", b).apply()
    }

    fun getIsOrderCancel(): Boolean = prefs.getBoolean("isOrderCancel", false)

    fun timeClear() {
        prefs.edit().remove("startOrderTime").apply()
        prefs.edit().remove("finishOrderTime").apply()
        prefs.edit().remove("transition_time").apply()
        saveIsCountingDown(true)
        prefs.edit().remove("is_timer_paused").apply()
    }

    fun getWaitTime(): Int {

        return prefs.getInt("current_wait_time", 0)
    }

    fun getTransitionTime(): Long {
        return prefs.getLong("transition_time", System.currentTimeMillis())

    }

    fun saveTransitionTime(transitionTime: Long) {
        prefs.edit().putLong("transition_time", transitionTime).apply()
    }

    fun saveIsPaused(value: Boolean) {
        prefs.edit().putBoolean("is_timer_paused", value).apply()
    }

    fun getIsPaused(): Boolean {
        return prefs.getBoolean("is_timer_paused", false)
    }

    fun savePauseTime(pauseTime: Long) {
        prefs.edit().putLong("pause_time", pauseTime).apply()
    }

    fun saveMessageValue(value: Int) {
        prefs.edit().putInt("message_value", value).apply()
    }

    fun saveMessageCount(count: Int) {
        prefs.edit().putInt("message_count", count).apply()
    }

    fun getMessageCount(): Int = prefs.getInt("message_count", 0)

    fun getMessageValue(): Int = prefs.getInt("message_value", 0)

    fun getPauseTime(): Long {
        return prefs.getLong("pause_time", 0L)
    }

    fun saveLastRaceId(raceId: Int) {
        prefs.edit().putInt(LAST_RACE_ID, raceId).apply()
    }

    fun saveLastRace(order: OrderCompleteRequest, raceId: Int) {
        prefs.edit().putInt(LAST_RACE_DISTANCE, order.distance).apply()
        prefs.edit().putInt(LAST_RACE_COST, order.cost).apply()
        prefs.edit().putInt(LAST_RACE_WAIT_COST, order.wait_cost).apply()
        prefs.edit().putInt(LAST_RACE_WAIT_TIME, order.wait_time).apply()
        prefs.edit().putInt(LAST_RACE_ID, raceId).apply()
    }

    fun getLastRaceId() = prefs.getInt(LAST_RACE_ID, -1)

    fun getLastRace(): OrderCompleteRequest {
        return OrderCompleteRequest(
            distance = prefs.getInt(LAST_RACE_DISTANCE, 0),
            cost = prefs.getInt(LAST_RACE_COST, 0),
            wait_cost = prefs.getInt(LAST_RACE_WAIT_COST, 0),
            wait_time = prefs.getInt(LAST_RACE_WAIT_TIME, 0),
            coordinates = emptyList()
        )
    }

    fun saveMapSettings(value: String, name: String) {
        prefs.edit().putString("preferred_map", value).apply()
        prefs.edit().putString("preferred_map_name", name).apply()
    }

    fun getMapSettings() =
        prefs.getString("preferred_map", "ru.yandex.yandexnavi") ?: "ru.yandex.yandexnavi"

    fun getMapName() =
        prefs.getString("preferred_map_name", "Yandex Navigator") ?: "Yandex Navigator"

    fun getOrderAppearance(): OrderAppearance {
        return when (prefs.getString("order_appearance", "small")) {
            "small" -> OrderAppearance.SMALL
            "large" -> OrderAppearance.LARGE
            else -> {
                OrderAppearance.SMALL
            }
        }
    }

    fun getCurrentDrive(): String?{
        return prefs.getString("currentDrive",null)
    }

    fun saveCurrentDrive(key: String, value:String) {
        with(prefs.edit()) {
            putString(key, value)
        }.apply()
    }

    fun savePaths(key1: String, value1: String, key2: String, value2: String) {
        prefs.edit().apply {
            putString(key1, value1)
            putString(key2, value2)
        }.apply()
    }


    fun getPath(key: String): String {
        return prefs.getString(key, "").toString()
    }

    fun clearPath() {
        prefs.edit().apply {
            remove("drivePath")
            remove("drivePathLite")
            remove("currentDrive")
        }.apply()
    }

    enum class OrderAppearance {
        SMALL, LARGE
    }
}