package com.example.taxi.utils

import android.util.Log

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type

object JsonUtils {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun <T> fromJson(json: String, type: Type): T? {
        val adapter: JsonAdapter<T> = moshi.adapter(type)
        return try {
            adapter.fromJson(json)
        } catch (e: JsonDataException) {
            Log.e("JsonUtils", "Error parsing JSON", e)
            null
        } catch (e: IOException) {
            Log.e("JsonUtils", "Error reading JSON", e)
            null
        }
    }

    fun parseJsonAndExtractValues(jsonString: String): Pair<String, Int> {
        val jsonObject = JSONObject(jsonString)
        val dataObject = jsonObject.getJSONObject("data")
        val addressObject = dataObject.getJSONObject("address")
        val fromValue = addressObject.getString("from")
        val startCostValue = dataObject.getInt("start_cost")
        return Pair(fromValue, startCostValue)
    }
}