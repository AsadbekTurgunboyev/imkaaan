package com.example.taxi.utils

import com.example.taxi.domain.model.MainResponse
import com.google.gson.Gson
import retrofit2.HttpException

object ErrorHandler {
    fun handle(e: Throwable): String {
        return if (e is HttpException) ({
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                mainResponse
            } catch (e: Exception) {
                "An error occurred"
            }
        }) as String else {
            "An error occurred"
        }
    }
}