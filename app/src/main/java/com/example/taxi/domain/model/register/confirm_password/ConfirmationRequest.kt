package com.example.taxi.domain.model.register.confirm_password
import com.google.gson.annotations.SerializedName

data class ConfirmationRequest(
    @SerializedName("token") val token: String,
    @SerializedName("code") val code: Int
)

data class ResendSmsRequest(
    @SerializedName("token") val token: String
)
