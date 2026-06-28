package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiInsight
import com.hyconnect.pleos.data.model.DashboardAction
import com.hyconnect.pleos.data.model.DashboardActionStyle
import com.hyconnect.pleos.data.model.DashboardActionType
import com.hyconnect.pleos.data.model.DashboardVehicle
import com.hyconnect.pleos.data.model.FuelStatus
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.InsightMetric
import com.hyconnect.pleos.data.model.MetricTone
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.data.model.SufficientDashboard
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.vehicle.habit.DrivingHabitProfile

object DummyHyConnectData {
    // 테스트용: 주행거리 500km(>임계값 100) + 잔량 83% → SUFFICIENT 모드로 진입해 연료 충분 대시보드를 보여준다.
    // LOW(충전소 추천) 화면을 테스트하려면 vehicleRangeKm를 100 이하로 내린다.
    val vehicleState = VehicleState(
        vehicleRangeKm = 500,
        fuelPercent = 83,
        message = "현재 경로 기준 충전 없이 약 5시간 주행할 수 있습니다.",
    )

    /**
     * 단독 실행/프리뷰용 더미 운전습관 프로파일.
     * 12회 주행·약 6시간 누적, 평균 78점(MODERATE) → 상단 점수 패널이 채워진 상태로 보인다.
     * (cumulativeScore = avgScore 78 × 12세션 = 936)
     * 누적 포인트 3,480P → Lv.4(베테랑 드라이버), 다음 레벨까지 520P.
     */
    val drivingHabit = DrivingHabitProfile(
        totalSessions = 12,
        totalDrivingMinutes = 372,
        harshAccelCount = 8,
        harshBrakeCount = 5,
        incautiousCount = 3,
        cumulativeScore = 936,
        totalPoints = 3_480,
        lastSessionPoints = 85,
        lastUpdatedEpochMs = 0L,
    )

    /** 서버 미연동 시 연료 충분 화면 검증용 데모 대시보드(예시 JSON과 동일 구성). */
    val sufficientDashboard = SufficientDashboard(
        vehicle = DashboardVehicle(
            fuelType = "hydrogen",
            fuelPercent = 83,
            rangeKm = 500,
        ),
        aiInsight = AiInsight(
            updatedAt = "2026-06-24T14:30:00+09:00",
            status = FuelStatus.SUFFICIENT,
            statusLabel = "수소 잔량 충분",
            subtitle = "지금 충전이 급하지 않아요",
            message = "현재 주행 패턴이라면 약 500km 더 주행할 수 있어요. 충전은 급하지 않으니, " +
                "이동 경로상 가장 저렴한 충전소를 미리 봐 두는 걸 추천해요.",
            metrics = listOf(
                InsightMetric(label = "주행 가능 거리", value = "500", unit = "km", tone = MetricTone.NEUTRAL),
                InsightMetric(label = "권장 충전 시점", value = "약 2시간 후", unit = null, tone = MetricTone.NEUTRAL),
                InsightMetric(label = "평균 소모율", value = "정상", unit = null, tone = MetricTone.POSITIVE),
            ),
        ),
        recommendedStation = RecommendedStationCard(
            stationId = "ST_SANGAM_001",
            name = "상암 수소충전소",
            address = "서울 마포구 상암동",
            badge = null,
            realtimePrice = true,
            distanceKm = 2.4,
            etaMinutes = 8,
            isOpen = true,
            waitingVehicles = 1,
            available = true,
            pricePerKg = 8250,
            priceDiffFromAvg = -430,
            estimatedCost = 41200,
            latitude = 37.5791,
            longitude = 126.8895,
        ),
        actions = listOf(
            DashboardAction(
                type = DashboardActionType.NAVIGATE,
                label = "경로 안내 시작",
                style = DashboardActionStyle.PRIMARY,
                stationId = "ST_SANGAM_001",
            ),
        ),
    )

    val recommendedStations = listOf(
        HydrogenStation(
            id = "1165020121HS2021004",
            name = "서울특별시 양재그린카스테이션(예약제)",
            address = "서울 서초구 바우뫼로12길 65",
            status = "운영 중",
            pressureInfo = "700bar 사용 가능",
            distanceKm = 7.96,
            waitMinutes = 0,
            isRecommended = true,
            latitude = 37.46833238084429,
            longitude = 127.03449982200269,
        ),
        HydrogenStation(
            id = "1114020121HS2022049",
            name = "서울특별시 서소문청사 수소충전소(사전 예약제)",
            address = "서울 중구 덕수궁길 15",
            status = "운영 중",
            pressureInfo = "350bar / 700bar",
            distanceKm = 19.72,
            waitMinutes = 0,
            latitude = 37.564050325721,
            longitude = 126.97451239126607,
        ),
        HydrogenStation(
            id = "4129020121HS2022038",
            name = "E1 과천 수소충전소",
            address = "경기 과천시 중앙로 526",
            status = "점검 예정",
            pressureInfo = "700bar",
            distanceKm = 8.65,
            waitMinutes = 0,
            latitude = 37.45571329817182,
            longitude = 127.00973560433977,
        ),
    )
}
