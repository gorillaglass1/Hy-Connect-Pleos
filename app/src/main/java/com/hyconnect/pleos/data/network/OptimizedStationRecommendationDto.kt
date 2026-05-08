package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// TODO: 시간/날짜 필드는 서버 포맷 확정 후 kotlinx-datetime 또는 java.time 변환으로 교체.
data class OptimizedStationRecommendationRequestDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("vehicle")
    val vehicle: OptimizedVehicleContextDto,
    @SerializedName("location")
    val location: OptimizedLocationDto,
    @SerializedName("navigation")
    val navigation: OptimizedNavigationContextDto?,
    @SerializedName("trigger")
    val trigger: OptimizedRecommendationTriggerDto,
    @SerializedName("preferences")
    val preferences: OptimizedRecommendationPreferencesDto,
    @SerializedName("candidate_stations")
    val candidateStations: List<OptimizedCandidateStationDto>,
)

data class OptimizedVehicleContextDto(
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("model")
    val model: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("remaining_hydrogen_percent")
    val remainingHydrogenPercent: Int,
    @SerializedName("remaining_range_km")
    val remainingRangeKm: Int,
    @SerializedName("tank_capacity_kg")
    val tankCapacityKg: Double?,
    @SerializedName("avg_efficiency_km_per_kg")
    val avgEfficiencyKmPerKg: Double?,
)

data class OptimizedLocationDto(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timestamp")
    val timestamp: String?,
)

data class OptimizedNavigationContextDto(
    @SerializedName("destination")
    val destination: OptimizedDestinationDto?,
    @SerializedName("remaining_route_distance_km")
    val remainingRouteDistanceKm: Double?,
    @SerializedName("estimated_arrival_time")
    val estimatedArrivalTime: String?,
    @SerializedName("estimated_remaining_range_at_arrival_km")
    val estimatedRemainingRangeAtArrivalKm: Int?,
    @SerializedName("route_polyline")
    val routePolyline: String?,
)

data class OptimizedDestinationDto(
    @SerializedName("name")
    val name: String?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
)

data class OptimizedRecommendationTriggerDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("range_threshold_km")
    val rangeThresholdKm: Int,
    @SerializedName("arrival_range_threshold_km")
    val arrivalRangeThresholdKm: Int,
    @SerializedName("fuel_threshold_percent")
    val fuelThresholdPercent: Int,
)

data class OptimizedRecommendationPreferencesDto(
    @SerializedName("prefer_700bar")
    val prefer700bar: Boolean,
    @SerializedName("max_detour_km")
    val maxDetourKm: Double,
    @SerializedName("prioritize")
    val prioritize: List<String>,
)

data class OptimizedCandidateStationDto(
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
    @SerializedName("distance_from_current_km")
    val distanceFromCurrentKm: Double?,
    @SerializedName("detour_distance_km")
    val detourDistanceKm: Double?,
    @SerializedName("is_on_route")
    val isOnRoute: Boolean,
    @SerializedName("price_per_kg")
    val pricePerKg: Int?,
    @SerializedName("payment_supported")
    val paymentSupported: String?,
    @SerializedName("realtime")
    val realtime: OptimizedRealtimeStationStatusDto?,
    @SerializedName("chargers")
    val chargers: List<OptimizedChargerCandidateDto>,
)

data class OptimizedRealtimeStationStatusDto(
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
    @SerializedName("updated_at")
    val updatedAt: String?,
)

data class OptimizedChargerCandidateDto(
    @SerializedName("hydrogen_charger_id")
    val hydrogenChargerId: Int,
    @SerializedName("charger_status")
    val chargerStatus: String,
    @SerializedName("hydrogen_pressure_bar")
    val hydrogenPressureBar: Int?,
    @SerializedName("pressure_type")
    val pressureType: String?,
)

data class OptimizedStationRecommendationResponseDto(
    @SerializedName("recommendation_id")
    val recommendationId: Long,
    @SerializedName("recommended_station")
    val recommendedStation: OptimizedRecommendedStationDto,
    @SerializedName("score")
    val score: Double,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("decision_factors")
    val decisionFactors: OptimizedDecisionFactorsDto,
    @SerializedName("recommendations")
    val recommendations: List<OptimizedStationRankDto>,
    @SerializedName("alternatives")
    val alternatives: List<OptimizedAlternativeStationDto>,
    @SerializedName("message_for_driver")
    val messageForDriver: String,
    @SerializedName("created_at")
    val createdAt: String?,
)

data class OptimizedRecommendedStationDto(
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
    @SerializedName("selected_charger_id")
    val selectedChargerId: Int?,
)

data class OptimizedStationRankDto(
    @SerializedName("rank")
    val rank: Int,
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
    @SerializedName("selected_charger_id")
    val selectedChargerId: Int?,
    @SerializedName("score")
    val score: Double,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("highlight")
    val highlight: String?,
    @SerializedName("decision_factors")
    val decisionFactors: OptimizedDecisionFactorsDto,
)

data class OptimizedAlternativeStationDto(
    @SerializedName("hydrogen_station_id")
    val hydrogenStationId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("score")
    val score: Double,
    @SerializedName("reason")
    val reason: String,
)

data class OptimizedDecisionFactorsDto(
    @SerializedName("reachable")
    val reachable: Boolean,
    @SerializedName("estimated_arrival_range_km")
    val estimatedArrivalRangeKm: Int,
    @SerializedName("detour_distance_km")
    val detourDistanceKm: Double,
    @SerializedName("estimated_wait_time_min")
    val estimatedWaitTimeMin: Int,
    @SerializedName("price_per_kg")
    val pricePerKg: Int?,
    @SerializedName("supports_700bar")
    val supports700bar: Boolean,
    @SerializedName("station_status")
    val stationStatus: String?,
)
