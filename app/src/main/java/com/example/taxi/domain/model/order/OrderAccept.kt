package com.example.taxi.domain.model.order

import com.example.taxi.domain.model.selfie.GenderModel
import com.google.gson.annotations.SerializedName

data class OrderAccept<U,T>(
    val id: Int,
    val type: Type,
    val services: List<Service>,
    val address: Address,
    val start_cost: Int,
    val latitude1: String,
    val longitude1: String,
    val latitude2: String?,
    val longitude2: String?,
    val comment: String?,
    val user: U,
    @SerializedName("mode") val mode: ModeModel,
    val activeHistory: Any?,
    val settings: List<Settings>,
    val currentPosition: Int,
    @SerializedName("arrived_at") val arrivedAt: Int? = null,
    @SerializedName("started_at") val startedAt: Int? = null,
    val mileage_prices: List<T>? = null
) {

    fun getSum(): Int {
        return services.sumOf { it.cost!! } + getMinCost()
    }

    fun getCostPerKm(): Int {
        val setting = settings.find { it.name == "cost_per_km" }
        return setting?.value?.toIntOrNull() ?: 0
    }

    fun getCostPerKmOutside(): Int {
        val settings = settings.find { it.name == "cost_out_center" }
        return settings?.value?.toIntOrNull() ?: 0
    }

    fun getMinDistance(): Int {
        val setting = settings.find { it.name == "min_distance" }
        return setting?.value?.toIntOrNull() ?: 0
    }

    fun getMinCost(): Int {
        val setting = settings.find { it.name == "cost_min_distance" }
        return setting?.value?.toIntOrNull() ?: 0
    }

    fun getMinWaitTime(): Int {
        val setting = settings.find { it.name == "min_wait_time" }
        return setting?.value?.toIntOrNull() ?: 0
    }

    fun getCostMinWaitTimePerMinute(): Int {
        val setting = settings.find { it.name == "cost_wait_time_per_minute" }
        return setting?.value?.toIntOrNull() ?: 0
    }
}

data class UserModel(
    val id: Int,
    val name: String,
    @SerializedName("gender") val gender: GenderModel,
    val phone: String
) {
    fun getPassengerPhone(): String {
        return phone
    }
}

data class MileageData(
    val id: Int,
    val mode_id: Int,
    val value: Int,
    val price: Int,
    val price_out_center: Int
)

data class ModeModel(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String
)

data class Settings(
    val id: String,
    val mode_id: String,
    val name: String,
    val info: String,
    val type: String,
    val value: String
)

enum class CostType {
    COST_PER_KM,
    COST_MIN_DISTANCE,
    MIN_WAIT_TIME,

}