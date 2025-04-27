package com.example.taxi.domain.model.order

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.getColor
import com.example.taxi.utils.ConversionUtil


data class OrderData<A>(
    val id: Int,
    val type: Type?,
    val services: List<Service>,
    val address: A,
    val start_cost: Int,
    val latitude1: String,
    val longitude1: String,
    val latitude2: String?,
    val longitude2: String?,
    val predict_cost: Int? = null,
    val predict_distance: Int? = null,
//    val created_at: CreatedAt,
    val comment: String?
)

data class CreatedAt(
    val timestamp: String,
    val date: String,
    val time: String
)

data class Service(
    val name: String? = "",
    val icon: String? = "",
    val cost: Int? = 0
)

data class Address(
    val from: String,
    val to: String
)

data class Type(
    val number: Int,
    val name: String
)


enum class TypeEnum(val textColor: String, val backgroundColor: String) {
    TYPE_DASHBOARD("#985F15", "#FFF3E3"),
    TYPE_TELEGRAM_BOT("#0C7AB9", "#E7F5FD"),
    TYPE_ANDROID_APP("#014EA8", "#EDF3FF")
}

fun updateTextView(type: Type, textView: AppCompatTextView) {
    val typeEnum = TypeEnum.values().find { it.ordinal + 1 == type.number }
    typeEnum?.let {
        textView.text = ConversionUtil.convertToCyrillic(type.name)
        textView.setTextColor(Color.parseColor(it.textColor))
        textView.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor(it.textColor))
        textView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it.backgroundColor))
    }
}

fun TextView.setDrawableColor(@ColorRes color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(getColor(context, color), PorterDuff.Mode.SRC_IN)
    }



}

