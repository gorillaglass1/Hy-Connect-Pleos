package com.hyconnect.pleos.data.model

data class HydrogenStation(
    val id: String,
    val name: String,
    val address: String,
    val status: String,
    val pressureInfo: String,
    val distanceKm: Double,
    val waitMinutes: Int,
    val isRecommended: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
