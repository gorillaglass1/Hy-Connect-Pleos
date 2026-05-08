package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class ChargingLogRequestDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("charged_amount")
    val chargedAmount: Double?,
    @SerializedName("charging_cost")
    val chargingCost: Double?,
    @SerializedName("waiting_time")
    val waitingTime: Int?,
)

data class ChargingLogResponseDto(
    @SerializedName("charging_log_id")
    val chargingLogId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("charged_amount")
    val chargedAmount: Double?,
    @SerializedName("charging_cost")
    val chargingCost: Double?,
    @SerializedName("waiting_time")
    val waitingTime: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
)
