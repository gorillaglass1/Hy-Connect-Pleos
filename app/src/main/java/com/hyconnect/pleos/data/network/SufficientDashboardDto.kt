package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 연료 충분(`battery_sufficient`) 화면의 서버 드리븐 UI 페이로드.
 * 서버 `POST /dashboard/sufficient` 응답과 1:1 대응한다.
 *
 * 설계 메모:
 * - 숫자(가격/거리/비용)는 포맷 없는 raw 값으로 받고 표시 포맷은 Compose에서 처리한다(다국어/단위 대응).
 * - [aiInsight].status / [actions].type 같은 문자열은 클라이언트에서 enum으로 파싱해 색상/분기를 결정한다.
 * - [screen]은 같은 스키마를 잔량 충분/부족 화면에 재사용할 때 분기용으로 둔다.
 */
data class SufficientDashboardDto(
    @SerializedName("screen")
    val screen: String?,
    @SerializedName("vehicle")
    val vehicle: DashboardVehicleDto?,
    @SerializedName("aiInsight")
    val aiInsight: AiInsightDto?,
    @SerializedName("recommendedStation")
    val recommendedStation: RecommendedStationCardDto?,
    @SerializedName("actions")
    val actions: List<DashboardActionDto> = emptyList(),
)

data class DashboardVehicleDto(
    @SerializedName("fuelType")
    val fuelType: String?,
    @SerializedName("fuelPercent")
    val fuelPercent: Int?,
    @SerializedName("rangeKm")
    val rangeKm: Int?,
)

data class AiInsightDto(
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("statusLabel")
    val statusLabel: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("metrics")
    val metrics: List<InsightMetricDto> = emptyList(),
)

data class InsightMetricDto(
    @SerializedName("label")
    val label: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("tone")
    val tone: String?,
)

data class RecommendedStationCardDto(
    @SerializedName("badge")
    val badge: String?,
    @SerializedName("realtimePrice")
    val realtimePrice: Boolean = false,
    @SerializedName("stationId")
    val stationId: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("distanceKm")
    val distanceKm: Double?,
    @SerializedName("etaMinutes")
    val etaMinutes: Int?,
    @SerializedName("isOpen")
    val isOpen: Boolean = true,
    @SerializedName("waitingVehicles")
    val waitingVehicles: Int?,
    @SerializedName("available")
    val available: Boolean = true,
    @SerializedName("pricePerKg")
    val pricePerKg: Int?,
    @SerializedName("priceDiffFromAvg")
    val priceDiffFromAvg: Int?,
    @SerializedName("estimatedCost")
    val estimatedCost: Int?,
    @SerializedName("location")
    val location: GeoLocationDto?,
)

data class GeoLocationDto(
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?,
)

data class DashboardActionDto(
    @SerializedName("type")
    val type: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("style")
    val style: String?,
    @SerializedName("stationId")
    val stationId: String?,
)
