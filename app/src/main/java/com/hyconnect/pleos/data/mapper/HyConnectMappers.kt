package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.network.RecommendationDeliveryPayloadDto
import com.hyconnect.pleos.data.network.RecommendedStationResponseDto
import kotlin.math.roundToInt

/**
 * 차량 전송용 flat 페이로드를 Compose UI 모델로 변환한다.
 * 좌표/가격/거리는 서버가 정제해 내려준 값을 그대로 쓴다.
 */
fun RecommendationDeliveryPayloadDto.toHydrogenStation(isRecommended: Boolean): HydrogenStation =
    HydrogenStation(
        id = chrstnMno,
        name = chrstnNm,
        address = roadNmAddr.orEmpty(),
        status = reachabilityStatus(isReachable),
        pressureInfo = facilities.toFacilityInfo(),
        distanceKm = (distanceToStation ?: 0.0).roundToOneDecimal(),
        waitMinutes = waitTimeMinutes ?: ((waitVehicles ?: 0) * 5),
        isRecommended = isRecommended,
        latitude = latitude,
        longitude = longitude,
    )

/**
 * 개인화 추천 응답(점수순 정렬)을 [StationRecommendation]으로 변환한다.
 * 1위 항목을 추천으로 표시하고, 그 추천 사유를 운전자 배너 문구로 사용한다.
 */
fun List<RecommendedStationResponseDto>.toStationRecommendation(): StationRecommendation {
    val top = firstOrNull()
    return StationRecommendation(
        driverMessage = top?.recommendationReason,
        stations = mapIndexed { index, dto ->
            dto.toHydrogenStation(isRecommended = index == 0)
        },
    )
}

fun RecommendedStationResponseDto.toHydrogenStation(isRecommended: Boolean): HydrogenStation =
    HydrogenStation(
        id = chrstnMno,
        name = chrstnNm,
        address = roadNmAddr.orEmpty(),
        status = reachabilityStatus(isReachable),
        pressureInfo = facilities.toFacilityInfo(),
        distanceKm = (distanceToStation ?: 0.0).roundToOneDecimal(),
        waitMinutes = waitTimeMinutes ?: ((waitVehicles ?: 0) * 5),
        isRecommended = isRecommended,
        latitude = deliveryPayload?.latitude,
        longitude = deliveryPayload?.longitude,
    )

/** 개인화 추천 1위 항목으로 AI 추천 카드 모델을 만든다. */
fun List<RecommendedStationResponseDto>.toAiRecommendation(): AiRecommendation {
    val top = firstOrNull()
    return AiRecommendation(
        title = top?.let { "${it.chrstnNm} 방문을 추천해요" } ?: "지금 충전하기 좋은 타이밍이에요",
        dustSummary = "",
        routeSummary = top?.recommendationReason.orEmpty(),
        reason = top?.recommendationReason
            ?: "대기 시간, 거리, 가격, 편의시설을 종합해 추천합니다.",
    )
}

private fun reachabilityStatus(isReachable: Boolean): String =
    if (isReachable) "도달 가능" else "도달 주의"

private fun List<String>.toFacilityInfo(): String =
    if (isEmpty()) "편의시설 정보 없음" else joinToString(" · ")

private fun Double.roundToOneDecimal(): Double = (this * 10).roundToInt() / 10.0
