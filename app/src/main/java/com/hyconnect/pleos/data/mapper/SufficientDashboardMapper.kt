package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.model.AiInsight
import com.hyconnect.pleos.data.model.DashboardAction
import com.hyconnect.pleos.data.model.DashboardActionStyle
import com.hyconnect.pleos.data.model.DashboardActionType
import com.hyconnect.pleos.data.model.DashboardVehicle
import com.hyconnect.pleos.data.model.FuelStatus
import com.hyconnect.pleos.data.model.InsightMetric
import com.hyconnect.pleos.data.model.MetricTone
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.data.model.SufficientDashboard
import com.hyconnect.pleos.data.network.AiInsightDto
import com.hyconnect.pleos.data.network.InsightMetricDto
import com.hyconnect.pleos.data.network.RecommendedStationCardDto
import com.hyconnect.pleos.data.network.SufficientDashboardDto
import kotlin.math.roundToInt

/**
 * 서버 드리븐 UI 페이로드를 Compose 표시용 모델로 변환한다.
 * 누락 필드는 안전한 기본값으로 채우고, 문자열 상태/타입은 enum으로 좁힌다.
 */
fun SufficientDashboardDto.toSufficientDashboard(): SufficientDashboard {
    val station = recommendedStation?.toRecommendedStationCard()
    return SufficientDashboard(
        vehicle = DashboardVehicle(
            fuelType = vehicle?.fuelType.orEmpty(),
            fuelPercent = (vehicle?.fuelPercent ?: 0).coerceIn(0, 100),
            rangeKm = vehicle?.remainingRange?.roundToInt() ?: 0,
        ),
        aiInsight = aiInsight.toAiInsight(),
        recommendedStation = station,
        actions = defaultActionsFor(station),
    )
}

private fun AiInsightDto?.toAiInsight(): AiInsight =
    AiInsight(
        // Gemini가 생성 시각을 내려주면 카드에 "HH:mm 업데이트"로 표기한다.
        updatedAt = this?.updatedAt,
        status = FuelStatus.from(this?.status),
        statusLabel = this?.statusLabel.orEmpty(),
        subtitle = this?.subtitle.orEmpty(),
        message = this?.message.orEmpty(),
        metrics = this?.metrics?.map { it.toInsightMetric() } ?: emptyList(),
    )

private fun InsightMetricDto.toInsightMetric(): InsightMetric =
    InsightMetric(
        label = label.orEmpty(),
        value = value.orEmpty(),
        unit = unit?.takeIf { it.isNotBlank() },
        // tone이 없으면 중립(강조 색 없음).
        tone = MetricTone.from(tone),
    )

private fun RecommendedStationCardDto.toRecommendedStationCard(): RecommendedStationCard =
    RecommendedStationCard(
        stationId = chrstnMno.orEmpty(),
        name = name.orEmpty(),
        address = roadNmAddr.orEmpty(),
        // 서버가 붙여준 뱃지가 있으면 유지하고, 없으면(빈 문자열 포함) 칩을 숨긴다.
        badge = badge?.takeIf { it.isNotBlank() },
        // 아래 두 값은 nearest-recommendation 응답에 없어 클라이언트 기본값을 쓴다.
        realtimePrice = false,
        etaMinutes = 0,
        available = true,
        distanceKm = distanceKm ?: 0.0,
        isOpen = isOpen,
        waitingVehicles = waitVehicles ?: 0,
        pricePerKg = ntslPc ?: 0,
        priceDiffFromAvg = priceDiffFromAvg?.roundToInt() ?: 0,
        estimatedCost = estimatedCost ?: 0,
        latitude = lat,
        longitude = lon,
    )

/**
 * 서버가 버튼(actions)을 더 이상 내려주지 않으므로, 추천소가 있으면 기본 버튼을 클라이언트에서 구성한다.
 * (경로 안내 = 추천소 선택)
 */
private fun defaultActionsFor(station: RecommendedStationCard?): List<DashboardAction> {
    if (station == null) return emptyList()
    return listOf(
        DashboardAction(
            type = DashboardActionType.NAVIGATE,
            label = "경로 안내 시작",
            style = DashboardActionStyle.PRIMARY,
            stationId = station.stationId,
        ),
    )
}
