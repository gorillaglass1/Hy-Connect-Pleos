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
import com.hyconnect.pleos.data.network.DashboardActionDto
import com.hyconnect.pleos.data.network.InsightMetricDto
import com.hyconnect.pleos.data.network.RecommendedStationCardDto
import com.hyconnect.pleos.data.network.SufficientDashboardDto

/**
 * 서버 드리븐 UI 페이로드를 Compose 표시용 모델로 변환한다.
 * 누락 필드는 안전한 기본값으로 채우고, 문자열 상태/타입은 enum으로 좁힌다.
 */
fun SufficientDashboardDto.toSufficientDashboard(): SufficientDashboard =
    SufficientDashboard(
        vehicle = DashboardVehicle(
            fuelType = vehicle?.fuelType.orEmpty(),
            fuelPercent = (vehicle?.fuelPercent ?: 0).coerceIn(0, 100),
            rangeKm = vehicle?.rangeKm ?: 0,
        ),
        aiInsight = aiInsight.toAiInsight(),
        recommendedStation = recommendedStation?.toRecommendedStationCard(),
        actions = actions.mapNotNull { it.toDashboardAction() },
    )

private fun AiInsightDto?.toAiInsight(): AiInsight =
    AiInsight(
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
        tone = MetricTone.from(tone),
    )

private fun RecommendedStationCardDto.toRecommendedStationCard(): RecommendedStationCard =
    RecommendedStationCard(
        stationId = stationId.orEmpty(),
        name = name.orEmpty(),
        address = address.orEmpty(),
        badge = badge?.takeIf { it.isNotBlank() },
        realtimePrice = realtimePrice,
        distanceKm = distanceKm ?: 0.0,
        etaMinutes = etaMinutes ?: 0,
        isOpen = isOpen,
        waitingVehicles = waitingVehicles ?: 0,
        available = available,
        pricePerKg = pricePerKg ?: 0,
        priceDiffFromAvg = priceDiffFromAvg ?: 0,
        estimatedCost = estimatedCost ?: 0,
        latitude = location?.lat,
        longitude = location?.lng,
    )

/** 타입을 못 알아보는(UNKNOWN) 액션은 렌더링하지 않도록 제외한다. */
private fun DashboardActionDto.toDashboardAction(): DashboardAction? {
    val actionType = DashboardActionType.from(type)
    if (actionType == DashboardActionType.UNKNOWN) return null
    return DashboardAction(
        type = actionType,
        label = label.orEmpty(),
        style = DashboardActionStyle.from(style),
        stationId = stationId,
    )
}
