package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 주행가능거리가 임계값 이하로 떨어졌을 때 자연어(nl_query)로 충전소 추천을 요청하는 페이로드.
 *
 * 클라이언트가 보내는 형식은 다음과 같다.
 * {
 *   "user_id": 1,
 *   "current_latitude": 37.934258,
 *   "current_longitude": 127.723832,
 *   "destination_latitude": 37.800006,
 *   "destination_longitude": 127.778294,
 *   "remaining_range": 100,
 *   "nl_query": "그냥 제일 가까운 충전소 젭알"
 * }
 */
data class NlStationRecommendationRequestDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("current_latitude")
    val currentLatitude: Double,
    @SerializedName("current_longitude")
    val currentLongitude: Double,
    @SerializedName("destination_latitude")
    val destinationLatitude: Double,
    @SerializedName("destination_longitude")
    val destinationLongitude: Double,
    @SerializedName("remaining_range")
    val remainingRange: Int,
    @SerializedName("nl_query")
    val nlQuery: String,
)

/**
 * 추천 응답. 실제 서버 스펙이 확정되면 필드를 맞춰 교체한다.
 * 핵심은 운전자에게 보여줄 한 줄 메시지(message_for_driver)와 추천 충전소 목록이다.
 */
data class NlStationRecommendationResponseDto(
    @SerializedName("query")
    val query: String?,
    @SerializedName("message_for_driver")
    val messageForDriver: String?,
    @SerializedName("recommended_stations")
    val recommendedStations: List<NlRecommendedStationDto> = emptyList(),
)

data class NlRecommendedStationDto(
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("distance_km")
    val distanceKm: Double?,
    @SerializedName("detour_km")
    val detourKm: Double?,
    @SerializedName("station_status")
    val stationStatus: String?,
    @SerializedName("pressure_info")
    val pressureInfo: String?,
    @SerializedName("wait_minutes")
    val waitMinutes: Int?,
    @SerializedName("price_per_kg")
    val pricePerKg: Int?,
    @SerializedName("is_recommended")
    val isRecommended: Boolean = false,
    @SerializedName("reason")
    val reason: String?,
)
