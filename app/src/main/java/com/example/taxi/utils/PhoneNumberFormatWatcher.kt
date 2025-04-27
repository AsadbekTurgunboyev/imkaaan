package com.example.taxi.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class PhoneNumberFormatWatcher(private val editText: TextInputEditText, private val inputLayout: TextInputLayout) : TextWatcher {
    private var current: String = ""
    private var isDeleting = false

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        val newString = s.toString()
        if (newString != current && !isDeleting) {
            val formatted = formatPhoneNumber(newString)
            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        } else if (isDeleting) {
            current = newString
        }
        if(s.toString().length == 12){
            inputLayout.error = null
        }
    }

    private fun formatPhoneNumber(input: String): String {
        val filteredInput = input.filter { it.isDigit() }
        val sb = StringBuilder()
        for ((i, char) in filteredInput.withIndex()) {
            sb.append(char)
            if (i == 1 || i == 4 || i == 6) {
                sb.append(' ')
            }
        }
        return sb.toString().uppercase(Locale.ROOT)
    }
}


fun createPhoneNumberPlateEditText(editText: TextInputEditText, inputLayout: TextInputLayout) {
    editText.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(12))
    editText.addTextChangedListener(PhoneNumberFormatWatcher(editText,inputLayout))
}
