package com.example.taxi.domain.model.register

import com.google.gson.annotations.SerializedName

data class RegisterRequest(@SerializedName("phone") val phone: String)
