package com.x.myapplication.data.model

data class RecommendationCard (
    val stationName: String,
    val distanceKm: Double,
    val isReachable: Boolean,
    val reason: String,
    val caution: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val poiId: String? = null,
    val poiSubId: String? = null,
    val address: String? = null,
)
