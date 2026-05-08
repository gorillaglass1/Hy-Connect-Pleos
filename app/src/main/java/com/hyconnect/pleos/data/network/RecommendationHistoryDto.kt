package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class RecommendationHistoryDto(
    @SerializedName("recommendation_id")
    val recommendationId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("recommendation_score")
    val recommendationScore: Double?,
    @SerializedName("recommendation_reason")
    val recommendationReason: String?,
    @SerializedName("user_latitude")
    val userLatitude: Double?,
    @SerializedName("user_longitude")
    val userLongitude: Double?,
    @SerializedName("vehicle_remaining_hydrogen")
    val vehicleRemainingHydrogen: Double?,
    @SerializedName("estimated_arrival_time")
    val estimatedArrivalTime: Int?,
    @SerializedName("selected")
    val selected: Boolean,
    @SerializedName("selected_at")
    val selectedAt: String?,
    @SerializedName("recommendation_type")
    val recommendationType: String?,
    @SerializedName("created_at")
    val createdAt: String?,
)
