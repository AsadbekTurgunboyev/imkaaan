package com.example.taxi.domain.model.selfie


data class SelfieAllData<T,S>(
    val id: Int,
    val username: String?,
    val phone: String,
    val first_name: String,
    val last_name: String,
    val auth_key: String,
    val photo: String?,
    val born: String,
    val gender: GenderModel,
    val type: TypeModel,
    val status: S,
    val step: StepModel,
    val is_advertising: Int?,
    val is_completed: T
)

data class GenderModel(
    val int: Int,
    val string: String
)

data class TypeModel(
    val int: Int,
    val string: String
)

data class StatusModel(
    val int: Int,
    var string: String
)

data class StepModel(
    val int: Int,
    val string: String
)

