package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class HydrogenStation(
    // TODO: 서버 /stations/recommended 응답 스키마가 확정되면 필드명과 타입을 실제 문서에 맞춘다.
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
