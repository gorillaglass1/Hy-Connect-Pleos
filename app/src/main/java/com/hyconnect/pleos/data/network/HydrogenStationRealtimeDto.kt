package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class HydrogenStationRealtimeDto(
    @SerializedName("realtime_id")
    val realtimeId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("available_chargers")
    val availableChargers: Int,
    @SerializedName("in_use_chargers")
    val inUseChargers: Int,
    @SerializedName("queue_count")
    val queueCount: Int,
    @SerializedName("avg_wait_time")
    val avgWaitTime: Int?,
    @SerializedName("hydrogen_stock_kg")
    val hydrogenStockKg: Double?,
    @SerializedName("station_status")
    val stationStatus: String?,
    @SerializedName("last_restock_at")
    val lastRestockAt: String?,
    @SerializedName("next_restock_schedule")
    val nextRestockSchedule: String?,
    @SerializedName("utilization_rate")
    val utilizationRate: Double?,
    @SerializedName("updated_at")
    val updatedAt: String?,
)
