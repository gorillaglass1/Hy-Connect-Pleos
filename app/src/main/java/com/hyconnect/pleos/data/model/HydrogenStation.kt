package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class HydrogenStation(
    // 서버 DTO는 data.network에 두고, 이 모델은 Compose UI 표시용으로 유지한다.
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("pressure_info")
    val pressureInfo: String,
    @SerializedName("distance_km")
    val distanceKm: Double,
    @SerializedName("wait_minutes")
    val waitMinutes: Int,
    @SerializedName("is_recommended")
    val isRecommended: Boolean = false,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null,
)
