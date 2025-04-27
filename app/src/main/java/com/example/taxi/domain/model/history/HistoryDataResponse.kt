package com.example.taxi.domain.model.history



data class HistoryDataResponse<M>(
    val _links: Links,
    val _meta : M,
    val data: List<Ride<RideType, RideAddress, RideUser, RideStatus, RideCreatedAt>>

)
data class Links(
    val self: Link?,
    val first: Link?,
    val last: Link?,
    val next: Link?
)

data class Link(
    val href: String?
)

data class Meta(
    val totalCount: Int?,
    val pageCount: Int?,
    val currentPage: Int?,
    val perPage: Int?
)


data class Ride<R,A,U,S,C>(
    val id: Int,
    val type: R,
    val order_id: Int,
    val services: List<Any>,
    val address: A,
    val latitude1: String,
    val longitude1: String,
    val latitude2: String?,
    val longitude2: String?,
    val comment: String?,
    val user: U,
    val mode: String,
    val distance: Int,
    val cost: Int,
    val status: S,
    val created_at: C
)

data class RideType(
    val number: Int,
    val name: String
)

data class RideAddress(
    val from: String,
    val to: String
)

data class RideUser(
    val id: Int,
    val name: String,
    val gender: RideGender,
    val phone: String
)

data class RideGender(
    val number: Int?,
    val name: String
)

data class RideStatus(
    val number: Int,
    val name: String
)

data class RideCreatedAt(
    val timestamp: String,
    val date: String,
    val datetime: String
)
