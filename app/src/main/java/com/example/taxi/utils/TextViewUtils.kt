package com.example.taxi.utils

import android.util.Log
import android.widget.TextView
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


fun TextView.setAddress(address: String){
    text = address.ifEmpty {
        "-"
    }
}

fun TextView.convertToCyrillic(content: String) {
    this.text = ConversionUtil.convertToCyrillic(content)
}

fun TextView.setPriceCost(number: Int) {
    Log.d("buyurtma", "setPriceCost: $number")
    val numberFormat = NumberFormat.getIntegerInstance(Locale.US) as DecimalFormat
    numberFormat.applyPattern("#,##0")
    val formattedNumber = numberFormat.format(number.toLong()).replace(",", " ")
    val t = "$formattedNumber UZS"
    text = t
    setTextIsSelectable(false)

}
