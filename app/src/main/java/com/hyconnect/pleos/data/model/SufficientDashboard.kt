package com.hyconnect.pleos.data.model

/**
 * 연료 충분 화면의 Compose 표시용 모델.
 * 네트워크 DTO(SufficientDashboardDto)를 매퍼로 변환해 만들며, 문자열 상태는 enum으로 좁혀
 * UI에서 색상/문구/아이콘을 안전하게 분기할 수 있게 한다.
 */
data class SufficientDashboard(
    val vehicle: DashboardVehicle,
    val aiInsight: AiInsight,
    val recommendedStation: RecommendedStationCard?,
    val actions: List<DashboardAction>,
)

data class DashboardVehicle(
    val fuelType: String,
    val fuelPercent: Int,
    val rangeKm: Int,
)

/** 잔량 상태. 카드 색상/아이콘/문구 분기에 사용한다. */
enum class FuelStatus {
    SUFFICIENT,
    RECOMMEND,
    URGENT,
    UNKNOWN;

    companion object {
        fun from(raw: String?): FuelStatus = when (raw?.lowercase()) {
            "sufficient" -> SUFFICIENT
            "recommend" -> RECOMMEND
            "urgent" -> URGENT
            else -> UNKNOWN
        }
    }
}

data class AiInsight(
    val updatedAt: String?,
    val status: FuelStatus,
    val statusLabel: String,
    val subtitle: String,
    val message: String,
    val metrics: List<InsightMetric>,
)

/** 지표 강조 톤. 값에 색을 입힐 때 사용한다. */
enum class MetricTone {
    POSITIVE,
    WARNING,
    NEUTRAL;

    companion object {
        fun from(raw: String?): MetricTone = when (raw?.lowercase()) {
            "positive" -> POSITIVE
            "warning", "negative" -> WARNING
            else -> NEUTRAL
        }
    }
}

data class InsightMetric(
    val label: String,
    val value: String,
    val unit: String?,
    val tone: MetricTone,
)

data class RecommendedStationCard(
    val stationId: String,
    val name: String,
    val address: String,
    val badge: String?,
    val realtimePrice: Boolean,
    val distanceKm: Double,
    val etaMinutes: Int,
    val isOpen: Boolean,
    val waitingVehicles: Int,
    val available: Boolean,
    val pricePerKg: Int,
    // 지역 평균 대비 가격 차이(원). 음수면 평균보다 저렴.
    val priceDiffFromAvg: Int,
    val estimatedCost: Int,
    val latitude: Double?,
    val longitude: Double?,
)

/** 서버가 내려준 버튼 동작 타입. */
enum class DashboardActionType {
    NAVIGATE,
    VIEW_MORE,
    UNKNOWN;

    companion object {
        fun from(raw: String?): DashboardActionType = when (raw?.lowercase()) {
            "navigate" -> NAVIGATE
            "viewmore", "view_more" -> VIEW_MORE
            else -> UNKNOWN
        }
    }
}

enum class DashboardActionStyle {
    PRIMARY,
    SECONDARY;

    companion object {
        fun from(raw: String?): DashboardActionStyle =
            if (raw?.lowercase() == "primary") PRIMARY else SECONDARY
    }
}

data class DashboardAction(
    val type: DashboardActionType,
    val label: String,
    val style: DashboardActionStyle,
    val stationId: String?,
)
