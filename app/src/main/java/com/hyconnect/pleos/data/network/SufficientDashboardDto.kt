package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 연료 충분(`battery_sufficient`) 대시보드 요청 페이로드.
 * 서버 `POST /nearest-recommendation` 요청 본문과 1:1 대응한다.
 *
 * 주의: 서버 계약상 위도 키는 `lat`이 아니라 `let`이다(오타가 아닌 서버 스펙).
 */
data class NearestRecommendationRequestDto(
    @SerializedName("vehicle")
    val vehicle: RequestVehicleDto,
    @SerializedName("location")
    val location: RequestLocationDto,
    @SerializedName("context")
    val context: RequestContextDto = RequestContextDto(),
)

data class RequestVehicleDto(
    @SerializedName("fuel_percent")
    val fuelPercent: Int,
    @SerializedName("remaining_range")
    val remainingRange: Double,
    @SerializedName("fuel_type")
    val fuelType: String,
)

data class RequestLocationDto(
    // 서버 스펙상 위도 키는 `let`이다.
    @SerializedName("let")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
)

data class RequestContextDto(
    @SerializedName("radius_km")
    val radiusKm: Int = 10,
)

/**
 * 연료 충분(`battery_sufficient`) 화면의 서버 드리븐 UI 페이로드.
 * 서버 `POST /nearest-recommendation` 응답과 1:1 대응한다.
 *
 * 설계 메모:
 * - 숫자(가격/거리/비용)는 포맷 없는 raw 값으로 받고 표시 포맷은 Compose에서 처리한다(다국어/단위 대응).
 * - [aiInsight].status 같은 문자열은 클라이언트에서 enum으로 파싱해 색상/분기를 결정한다.
 * - [screen]은 같은 스키마를 잔량 충분/부족 화면에 재사용할 때 분기용으로 둔다.
 * - 응답에는 버튼(actions)이 없다. 추천소가 있을 때 기본 액션은 매퍼에서 클라이언트가 구성한다.
 */
data class SufficientDashboardDto(
    @SerializedName("screen")
    val screen: String?,
    @SerializedName("vehicle")
    val vehicle: DashboardVehicleDto?,
    @SerializedName("ai_insight")
    val aiInsight: AiInsightDto?,
    @SerializedName("recommended_station")
    val recommendedStation: RecommendedStationCardDto?,
)

data class DashboardVehicleDto(
    @SerializedName("fuel_type")
    val fuelType: String?,
    @SerializedName("fuel_percent")
    val fuelPercent: Int?,
    @SerializedName("remaining_range")
    val remainingRange: Double?,
)

data class AiInsightDto(
    @SerializedName("status")
    val status: String?,
    @SerializedName("status_label")
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
)

data class RecommendedStationCardDto(
    @SerializedName("chrstn_mno")
    val chrstnMno: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("road_nm_addr")
    val roadNmAddr: String?,
    @SerializedName("distance_km")
    val distanceKm: Double?,
    @SerializedName("ntsl_pc")
    val ntslPc: Int?,
    @SerializedName("price_diff_from_avg")
    val priceDiffFromAvg: Double?,
    @SerializedName("estimated_cost")
    val estimatedCost: Int?,
    @SerializedName("wait_vhcle_alge")
    val waitVehicles: Int?,
    @SerializedName("is_open")
    val isOpen: Boolean = true,
    // 서버 스펙상 위도 키는 `let`이다.
    @SerializedName("let")
    val lat: Double?,
    @SerializedName("lon")
    val lon: Double?,
)
