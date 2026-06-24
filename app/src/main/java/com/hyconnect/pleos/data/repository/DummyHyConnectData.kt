package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState

object DummyHyConnectData {
    val vehicleState = VehicleState(
        vehicleRangeKm = 100,
        message = "현재 경로 기준 충전 없이 약 1시간 40분 주행할 수 있습니다.",
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
