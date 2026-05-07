package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import kotlinx.coroutines.delay

interface HyConnectRepository {
    suspend fun getVehicleState(): VehicleState
    suspend fun getAiRecommendation(): AiRecommendation
    suspend fun getRecommendedStations(): List<HydrogenStation>
}

class MockHyConnectRepository : HyConnectRepository {
    override suspend fun getVehicleState(): VehicleState {
        delay(120L)
        // TODO: VehicleState를 실제 Pleos Vehicle SDK의 연료/수소 잔량 데이터와 연결한다.
        return VehicleState()
    }

    override suspend fun getAiRecommendation(): AiRecommendation {
        delay(120L)
        return AiRecommendation()
    }

    override suspend fun getRecommendedStations(): List<HydrogenStation> {
        delay(120L)
        // TODO: 현재 위치를 Pleos FusedLocation SDK 또는 차량 위치 API와 연결해 주변 충전소 기준점을 교체한다.
        return listOf(
            HydrogenStation(
                id = "hyundai-yangjae",
                name = "현대 수소충전소 양재",
                address = "경기 성남시 분당구 문로 12 길 123",
                status = "운영 중",
                pressureInfo = "700bar 사용 가능",
                distanceKm = 2.1,
                waitMinutes = 5,
                isRecommended = true,
            ),
            HydrogenStation(
                id = "gaia",
                name = "가이아 수소충전소",
                address = "경기 성남시 분당구 구룡로 289",
                status = "운영 중",
                pressureInfo = "700bar 사용 가능",
                distanceKm = 4.5,
                waitMinutes = 10,
            ),
            HydrogenStation(
                id = "hyundai-pangyo",
                name = "현대 수소충전소 판교",
                address = "경기 성남시 분당구 고등로 98",
                status = "운영 중",
                pressureInfo = "700bar 사용 가능",
                distanceKm = 8.7,
                waitMinutes = 15,
            ),
        )
    }
}
