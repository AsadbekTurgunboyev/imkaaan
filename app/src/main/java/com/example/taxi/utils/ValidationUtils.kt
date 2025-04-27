package com.example.taxi.utils

object ValidationUtils {
    private const val NAME_PATTERN = "^[\\p{L} .'-]+$"
    private const val FULL_NAME_PATTERN = "^[\\p{L} .'-]+ [\\p{L} .'-]+$"

    fun isValidName(name: String): Boolean = name.matches(Regex(NAME_PATTERN))
    fun isValidFullName(fullName: String): Boolean = fullName.matches(Regex(FULL_NAME_PATTERN))
    fun isValidBirthday(birthday: String): Boolean = birthday.isNotEmpty()
    fun isValidCarName(carName: String): Boolean = carName.isNotEmpty()
    fun isValidPhoneNumber(phone: String): Boolean = phone.length == 12
    fun isValidCarColor(color: String): Boolean = color.isNotEmpty()
    fun isValidCarPosition(position: String): Boolean = position.isNotEmpty()
    fun isValidCarFirstTwoNumber(num1: String): Boolean = num1.length == 2
    fun isValidCarMainNumber(number: String): Boolean = number.length <= 10
    fun isValidCarPassport(passport: String): Boolean = passport.length <= 14
}