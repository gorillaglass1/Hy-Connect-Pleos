package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState

object DummyHyConnectData {
    val vehicleState = VehicleState(
        hydrogenPercent = 23,
        vehicleRangeKm = 100,
        message = "현재 경로 기준 충전 없이 약 1시간 40분 주행할 수 있습니다.",
    )

    val aiRecommendation = AiRecommendation(
        title = "양재 충전소를 먼저 방문하세요",
        dustSummary = "강남권 미세먼지는 보통이며 외기 순환 주행이 가능합니다.",
        routeSummary = "경부고속도로 진입 전 충전하면 예상 대기 시간이 가장 짧습니다.",
        reason = "잔여 수소량, 현재 위치, 대기 시간, 700bar 가능 여부를 함께 반영했습니다.",
    )

    val recommendedStations = listOf(
        HydrogenStation(
            id = "1",
            name = "현대 수소충전소 양재",
            address = "서울 서초구 바우뫼로 12길 123",
            status = "운영 중",
            pressureInfo = "700bar 사용 가능",
            distanceKm = 2.1,
            waitMinutes = 5,
            isRecommended = true,
            latitude = 37.468164,
            longitude = 127.038703,
        ),
        HydrogenStation(
            id = "2",
            name = "H 강동 수소스테이션",
            address = "서울 강동구 천호대로 1452",
            status = "운영 중",
            pressureInfo = "350bar / 700bar",
            distanceKm = 9.8,
            waitMinutes = 12,
            latitude = 37.545762,
            longitude = 127.170278,
        ),
        HydrogenStation(
            id = "3",
            name = "수원 영통 수소충전소",
            address = "경기 수원시 영통구 광교호수로 250",
            status = "점검 예정",
            pressureInfo = "700bar",
            distanceKm = 24.6,
            waitMinutes = 18,
            latitude = 37.283755,
            longitude = 127.065463,
        ),
    )
}
