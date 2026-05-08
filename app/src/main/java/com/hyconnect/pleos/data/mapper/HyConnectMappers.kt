package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.HydrogenChargerDto
import com.hyconnect.pleos.data.network.HydrogenStationDto
import com.hyconnect.pleos.data.network.HydrogenStationRealtimeDto
import com.hyconnect.pleos.data.network.OptimizedStationRankDto
import com.hyconnect.pleos.data.network.OptimizedStationRecommendationResponseDto
import com.hyconnect.pleos.data.network.RecommendationHistoryDto
import com.hyconnect.pleos.data.network.VehicleDto
import com.hyconnect.pleos.utils.calculateDistanceKm
import kotlin.math.roundToInt

fun HydrogenStationDto.toDomain(
    realtime: HydrogenStationRealtimeDto?,
    chargers: List<HydrogenChargerDto>,
    isRecommended: Boolean,
    currentLat: Double?,
    currentLng: Double?,
): HydrogenStation {
    val distanceKm = if (currentLat != null && currentLng != null) {
        calculateDistanceKm(currentLat, currentLng, latitude, longitude)
    } else {
        0.0
    }

    return HydrogenStation(
        id = hydrogenStationId.toString(),
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        status = realtime?.stationStatus ?: "상태 정보 없음",
        pressureInfo = chargers.toPressureInfo(),
        waitMinutes = realtime?.avgWaitTime ?: ((realtime?.queueCount ?: 0) * 5),
        isRecommended = isRecommended,
        distanceKm = (distanceKm * 10).roundToInt() / 10.0,
    )
}

fun List<HydrogenStationDto>.mergeWithRealtimeAndChargers(
    realtimeList: List<HydrogenStationRealtimeDto>,
    chargerList: List<HydrogenChargerDto>,
    recommendationHistories: List<RecommendationHistoryDto>,
    currentLat: Double?,
    currentLng: Double?,
): List<HydrogenStation> {
    val realtimeByStationId = realtimeList.associateBy { it.hydrogenStationId }
    val chargersByStationId = chargerList.groupBy { it.hydrogenStationId }
    val recommendedStationId = recommendationHistories
        .maxByOrNull { it.recommendationScore ?: Double.NEGATIVE_INFINITY }
        ?.hydrogenStationId

    val mappedStations = map { station ->
        val realtime = realtimeByStationId[station.hydrogenStationId]
        station.toDomain(
            realtime = realtime,
            chargers = chargersByStationId[station.hydrogenStationId].orEmpty(),
            isRecommended = recommendedStationId == station.hydrogenStationId,
            currentLat = currentLat,
            currentLng = currentLng,
        )
    }

    val stationsWithFallbackRecommendation = if (recommendedStationId == null) {
        val fallbackId = mappedStations.minWithOrNull(
            compareBy<HydrogenStation> { it.waitMinutes }.thenBy { it.distanceKm },
        )?.id
        mappedStations.map { station ->
            station.copy(isRecommended = station.id == fallbackId)
        }
    } else {
        mappedStations
    }

    return stationsWithFallbackRecommendation.sortedWith(
        compareByDescending<HydrogenStation> { it.isRecommended }
            .thenBy { it.waitMinutes }
            .thenBy { it.distanceKm },
    )
}

fun VehicleDto.toVehicleState(hydrogenPercent: Int = 78): VehicleState {
    // TODO: 실시간 수소 잔량은 Pleos Vehicle SDK 또는 별도 서버 API가 확정되면 교체.
    // TODO: Vehicle SDK 연동 시 Vehicle(context) initialize/release 라이프사이클을 별도 DataSource로 분리.
    val calculatedRangeKm = avgEfficiency?.let { efficiency ->
        (tankCapacity * efficiency * hydrogenPercent / 100).roundToInt()
    } ?: 500

    return VehicleState(
        hydrogenPercent = hydrogenPercent,
        vehicleRangeKm = calculatedRangeKm,
        message = "서버 차량 등록 정보와 임시 수소 잔량을 기준으로 계산했습니다.",
    )
}

fun RecommendationHistoryDto.toAiRecommendation(
    stationName: String?,
): AiRecommendation =
    AiRecommendation(
        title = "${stationName ?: "추천 충전소"} 방문을 추천해요",
        dustSummary = "",
        routeSummary = "",
        reason = recommendationReason
            ?: "대기 시간, 충전기 상태, 700bar 지원 여부를 기준으로 추천했습니다.",
    )

fun OptimizedStationRecommendationResponseDto.toAiRecommendation(): AiRecommendation =
    AiRecommendation(
        title = "${recommendedStation.name} 방문을 추천해요",
        dustSummary = "",
        routeSummary = messageForDriver,
        reason = reason,
    )

fun OptimizedStationRecommendationResponseDto.toRecommendedStations(): List<HydrogenStation> =
    recommendations
        .sortedBy { it.rank }
        .map { recommendation ->
            recommendation.toHydrogenStation(isRecommended = recommendation.rank == 1)
        }

private fun OptimizedStationRankDto.toHydrogenStation(isRecommended: Boolean): HydrogenStation =
    HydrogenStation(
        id = hydrogenStationId.toString(),
        name = name,
        address = address,
        status = decisionFactors.stationStatus ?: "상태 정보 없음",
        pressureInfo = if (decisionFactors.supports700bar) {
            "700bar 사용 가능"
        } else {
            "압력 정보 없음"
        },
        distanceKm = decisionFactors.detourDistanceKm,
        waitMinutes = decisionFactors.estimatedWaitTimeMin,
        isRecommended = isRecommended,
        latitude = latitude,
        longitude = longitude,
    )

private fun List<HydrogenChargerDto>.toPressureInfo(): String {
    if (any { it.hydrogenPressureBar == 700 || it.pressureType.contains("700", ignoreCase = true) }) {
        return "700bar 사용 가능"
    }

    return firstOrNull()?.pressureType ?: "압력 정보 없음"
}
