package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class VehicleDto(
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("vehicle_number")
    val vehicleNumber: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("vehicle_type")
    val vehicleType: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("tank_capacity")
    val tankCapacity: Double,
    @SerializedName("avg_efficiency")
    val avgEfficiency: Double?,
    @SerializedName("registered_at")
    val registeredAt: String?,
)
