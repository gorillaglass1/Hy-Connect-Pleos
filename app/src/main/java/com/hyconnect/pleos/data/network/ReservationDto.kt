package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class ReservationRequestDto(
    @SerializedName("hydrogen_charger_id")
    val hydrogenChargerId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("reservation_status")
    val reservationStatus: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("reservation_time")
    val reservationTime: String,
    @SerializedName("expire_time")
    val expireTime: String,
)

data class ReservationResponseDto(
    @SerializedName("hydrogen_station_reservation_id")
    val hydrogenStationReservationId: Int,
    @SerializedName("hydrogen_charger_id")
    val hydrogenChargerId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("reservation_status")
    val reservationStatus: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("reservation_time")
    val reservationTime: String,
    @SerializedName("expire_time")
    val expireTime: String,
    @SerializedName("created_at")
    val createdAt: String?,
)
