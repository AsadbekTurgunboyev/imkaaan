package com.example.taxi.utils

import android.text.Editable
import android.text.TextWatcher
import com.chaos.view.PinView

object PinViewUtils {
    fun setPinViewTextChangedListener(
        pinView: PinView,
        callback: (pin: String) -> Unit
    ) {
        pinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    val pin = s.toString()
                    callback(pin)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }
}
