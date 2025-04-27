package com.example.taxi.domain.model.register



data class RegisterData(
    val phone: String,
    val message: String,
    val token: String,
    val type: Type
)

data class Type(
    val number: Int,
    val name: String
)