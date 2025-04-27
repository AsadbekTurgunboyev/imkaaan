package com.example.taxi.utils

import com.example.taxi.domain.model.MainResponse
import com.google.gson.Gson
import retrofit2.HttpException

fun getMainErrorMessage(error: Throwable): String {
    return if (error is HttpException) {
        try {
            val errorBody = error.response()?.errorBody()?.string()
            val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
            mainResponse?.message ?: "An error occurred"
        } catch (e: Exception) {
            "An error occurred"
        }
    } else {
        "An error occurred"
    }
}