package com.example.taxi.domain.model.register.confirm_password

import com.google.gson.annotations.SerializedName

data class UserData<I>(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("phone") val phone: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("auth_key") val authKey: String,
    @SerializedName("photo") val photo: String?,
    @SerializedName("born") val born: String?,
    @SerializedName("gender") val gender: GenderData,
    @SerializedName("type") val type: TypeData,
    @SerializedName("status") val status: StatusData,
    @SerializedName("step") val stepData: StepData,
    @SerializedName("is_completed") val isCompleted: I
)

data class GenderData(
    @SerializedName("int") val int: Int?,
    @SerializedName("string") val string: String
)

data class TypeData(
    @SerializedName("int") val int: Int,
    @SerializedName("string") val string: String
)

data class StatusData(
    @SerializedName("int") val int: Int,
    @SerializedName("string") val string: String
)

data class StepData(
    @SerializedName("int") val int: Int,
    @SerializedName("string") val string: String
)

data class IsCompletedData(
    @SerializedName("int") val int: Int,
    @SerializedName("string") val string: Boolean
)