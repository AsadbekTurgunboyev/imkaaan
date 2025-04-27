package com.example.taxi.domain.model.register.person_data

data class PersonDataResponse(
    val success: Boolean,
    val status: Int,
    val name: String,
    val message: String,
    val step: Int,
    val data: UserData
)

data class UserData(
    val id: Int,
    val username: String?,
    val phone: String,
    val first_name: String,
    val last_name: String,
    val auth_key: String,
    val photo: String?,
    val born: String,
    val gender: Gender,
    val type: UserType,
    val status: UserStatus,
    val step: UserStep,
    val is_completed: IsCompleted
)

data class Gender(
    val int: Int,
    val string: String
)

data class UserType(
    val int: Int,
    val string: String
)

data class UserStatus(
    val int: Int,
    val string: String
)

data class UserStep(
    val int: Int,
    val string: String
)

data class IsCompleted(
    val int: Int,
    val string: Boolean
)