package com.example.taxi.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class CarNumberPlateFormatWatcher(private val editText: TextInputEditText) : TextWatcher {
    private var current: String = ""
    private var isDeleting = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        isDeleting = count > after
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val newString = s.toString()
        if (newString != current && !isDeleting) {
            val formatted = formatCarNumberPlate(s.toString())
            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        }else if (isDeleting) {
            current = newString
        }

    }


    private fun formatCarNumberPlate(input: String): String {
        val filteredInput = input.filter { it.isLetterOrDigit() }
        val sb = StringBuilder()
        var i = 0
        while (i < filteredInput.length) {
            sb.append(filteredInput[i])
            if (i == 0 || i == 3) {
                sb.append(' ')
            }
            if ((i == 0 || i == 4 || i == 5) && !filteredInput[i].isLetter()) {
                // Reject input if {2,6,7} character is not a letter
                return current
            }

            if ((i == 1 || i == 2 || i == 3) && !filteredInput[i].isDigit()) {
                // Reject input if {3,4,5} character is not a letter
                return current
            }
//
            i++
        }
        return sb.toString().uppercase()
    }
}

class CarLicencePlateFormatWatcher(private val editText: TextInputEditText) : TextWatcher {
    private var current: String = ""

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            val formatted = formatCarLicencePlate(s.toString())
            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)
        }
    }

    private fun formatCarLicencePlate(input: String): String {
        val filteredInput = input.filter { it.isLetterOrDigit() }
        val sb = StringBuilder()
        var i = 0
        while (i < filteredInput.length) {
            sb.append(filteredInput[i])
            if ((i in 0..4) && !filteredInput[i].isDigit()) {
                // Reject input if {2,6,7} character is not a letter
                return current
            }
            if ((i in 5..7) && !filteredInput[i].isLetter()) {
                // Reject input if {3,4,5} character is not a letter
                return current
            }
//
            i++
        }
        return sb.toString().uppercase()
    }

}

fun createCarNumberPlateEditText(editText: TextInputEditText) {
    editText.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(8))
    editText.addTextChangedListener(CarNumberPlateFormatWatcher(editText))
}

fun createCarLicencePlateEdittext(editText: TextInputEditText) {
    editText.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(8))
    editText.addTextChangedListener(CarLicencePlateFormatWatcher(editText))
}

fun nextChangeEdittext(
    editText: TextInputEditText,
    move: () -> Unit = {}
) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (editText.text.toString().length == 2) {
                move()
            }
        }

        override fun afterTextChanged(p0: Editable?) {

        }
    })
}
