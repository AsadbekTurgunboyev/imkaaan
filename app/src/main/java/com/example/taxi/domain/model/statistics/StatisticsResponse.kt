package com.example.taxi.domain.model.statistics

data class StatisticsResponse<T>(
    val period_between_date: String,
    val data: T
)

data class StatisticsResponseValue(
    val totalSum: Int,
    val totalCount: Int,
    val distance: Int,
    val total: Int
)
