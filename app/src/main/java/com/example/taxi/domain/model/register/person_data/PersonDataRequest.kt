package com.example.taxi.domain.model.register.person_data

import com.google.gson.annotations.SerializedName

data class PersonDataRequest(
    @SerializedName("first_name") val first_name: String,
    @SerializedName("last_name") val last_name: String,
    @SerializedName("born") val born: String,
    @SerializedName("gender") val gender: Int
)