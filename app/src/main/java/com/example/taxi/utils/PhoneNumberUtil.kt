package com.example.taxi.utils

class PhoneNumberUtil {
    companion object {
        fun formatPhoneNumber(phoneNumber: String?, countryCode: String?): String? {
            if (phoneNumber.isNullOrEmpty() || countryCode.isNullOrEmpty()) {
                return null
            }
            // Format the phone number
            return phoneNumber

        }

        fun formatMoneyNumberPlate(input: String): String {
            val regex = "(\\d)(?=(\\d{3})+$)".toRegex()
            val a =  input.replace(regex, "$1 ")
            return "$a UZS"
        }
    }
}