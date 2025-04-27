package com.example.taxi.domain.model.transfer

import com.google.gson.annotations.SerializedName


data class ResponseTransferHistory<H>(
    @SerializedName("data") val data: List<DataItem<HistoryType, HistoryCreatedAt>>,
    @SerializedName("_links") val links: Links,
    @SerializedName("_meta") val meta: H
)

data class DataItem<T, U>(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: T,
    @SerializedName("value") val value: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("reason") val reason: String,
    @SerializedName("comment") val comment: String?,
    @SerializedName("created_at") val createdAt: U
)


data class HistoryCreatedAt(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("date") val date: String,
    @SerializedName("datetime") val datetime: String
)

data class HistoryType(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String
)

data class Links(
    @SerializedName("self") val self: Self,
    @SerializedName("first") val first: First,
    @SerializedName("last") val last: Last,
    @SerializedName("next") val next: Next
)

data class Self(
    @SerializedName("href") val href: String
)

data class First(
    @SerializedName("href") val href: String
)

data class Last(
    @SerializedName("href") val href: String
)

data class Next(
    @SerializedName("href") val href: String
)

data class HistoryMeta(
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("pageCount") val pageCount: Int,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("perPage") val perPage: Int
)
