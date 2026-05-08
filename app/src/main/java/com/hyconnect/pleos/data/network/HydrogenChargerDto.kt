package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class HydrogenChargerDto(
    @SerializedName("hydrogen_charger_id")
    val hydrogenChargerId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("charger_status")
    val chargerStatus: String,
    @SerializedName("charger_type")
    val chargerType: String?,
    @SerializedName("hydrogen_pressure_bar")
    val hydrogenPressureBar: Int?,
    @SerializedName("pressure_type")
    val pressureType: String,
    @SerializedName("restock_schedule")
    val restockSchedule: String?,
)
