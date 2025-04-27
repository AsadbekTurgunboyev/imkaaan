package com.example.taxi.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

object EditTextIdUtils {

    fun setEditTextChangedListener(
        pinView: EditText,
        callback: (pin: String) -> Unit
    ) {
        pinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val id = s.toString()
                callback(id)

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    fun setEditTextListenerForButton(
        editText: EditText,
        callback: (pin: String) -> Unit
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                callback(s.toString())
            }
        })
    }
}