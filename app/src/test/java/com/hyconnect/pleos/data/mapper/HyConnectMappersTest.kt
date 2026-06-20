package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.network.RecommendationDeliveryPayloadDto
import com.hyconnect.pleos.data.network.RecommendedStationResponseDto
import com.hyconnect.pleos.data.network.SubScoresDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HyConnectMappersTest {
    @Test
    fun deliveryPayloadMapsToHydrogenStation() {
        val station = payloadDto().toHydrogenStation(isRecommended = true)

        assertEquals("2820020121HS2019018", station.id)
        assertEquals("H인천수소충전소 에코스테이션", station.name)
        assertEquals("인천 남동구 청능대로468번길 1", station.address)
        assertEquals("도달 가능", station.status)
        assertEquals("대기실 · 세차장 · 편의점", station.pressureInfo)
        assertEquals(175.8, station.distanceKm, 0.0)
        assertEquals(0, station.waitMinutes)
        assertTrue(station.isRecommended)
        assertEquals(37.39867905, station.latitude ?: 0.0, 0.0)
        assertEquals(126.71148794, station.longitude ?: 0.0, 0.0)
    }

    @Test
    fun recommendationListMapsToStationRecommendation() {
        val recommendation = listOf(
            responseDto(chrstnMno = "A", reason = "1순위 사유", reachable = true),
            responseDto(chrstnMno = "B", reason = "2순위 사유", reachable = false),
        ).toStationRecommendation()

        assertEquals("1순위 사유", recommendation.driverMessage)
        assertEquals(2, recommendation.stations.size)
        assertTrue(recommendation.stations.first().isRecommended)
        assertFalse(recommendation.stations[1].isRecommended)
        assertEquals("도달 주의", recommendation.stations[1].status)
    }

    @Test
    fun recommendationListMapsToAiRecommendation() {
        val ai = listOf(responseDto(chrstnMno = "A", reason = "가장 매칭도가 높음", reachable = true))
            .toAiRecommendation()

        assertTrue(ai.title.contains("방문을 추천해요"))
        assertEquals("가장 매칭도가 높음", ai.reason)
        assertEquals("가장 매칭도가 높음", ai.routeSummary)
    }

    @Test
    fun waitMinutesFallsBackToWaitVehiclesTimesFive() {
        val station = payloadDto(waitTimeMinutes = null, waitVehicles = 4)
            .toHydrogenStation(isRecommended = false)

        assertEquals(20, station.waitMinutes)
    }

    private fun payloadDto(
        waitTimeMinutes: Int? = 0,
        waitVehicles: Int? = 0,
    ) = RecommendationDeliveryPayloadDto(
        chrstnMno = "2820020121HS2019018",
        chrstnNm = "H인천수소충전소 에코스테이션",
        roadNmAddr = "인천 남동구 청능대로468번길 1",
        latitude = 37.39867905,
        longitude = 126.71148794,
        vhcleKndCd = null,
        vhcleKndNm = null,
        ntslPc = 11000,
        distanceToStation = 175.82,
        detourDistance = 0.36,
        waitVehicles = waitVehicles,
        waitTimeMinutes = waitTimeMinutes,
        facilities = listOf("대기실", "세차장", "편의점"),
        isReachable = true,
        finalScore = 84.1,
        recommendationReason = "매칭도가 매우 높습니다.",
    )

    private fun responseDto(
        chrstnMno: String,
        reason: String,
        reachable: Boolean,
    ) = RecommendedStationResponseDto(
        chrstnMno = chrstnMno,
        chrstnNm = "충전소 $chrstnMno",
        roadNmAddr = "주소 $chrstnMno",
        vhcleKndCd = null,
        vhcleKndNm = null,
        ntslPc = 11000,
        distanceToStation = 175.82,
        distanceToDestination = 12.4,
        detourDistance = 0.36,
        waitVehicles = 0,
        waitTimeMinutes = 0,
        facilities = listOf("대기실"),
        isReachable = reachable,
        subScores = SubScoresDto(price = 70.0, waitingTime = 100.0, distance = 95.0, facilities = 60.0),
        finalScore = 84.1,
        recommendationReason = reason,
        deliveryPayload = RecommendationDeliveryPayloadDto(
            chrstnMno = chrstnMno,
            chrstnNm = "충전소 $chrstnMno",
            roadNmAddr = "주소 $chrstnMno",
            latitude = 37.4,
            longitude = 126.7,
            vhcleKndCd = null,
            vhcleKndNm = null,
            ntslPc = 11000,
            distanceToStation = 175.82,
            detourDistance = 0.36,
            waitVehicles = 0,
            waitTimeMinutes = 0,
            facilities = listOf("대기실"),
            isReachable = reachable,
            finalScore = 84.1,
            recommendationReason = reason,
        ),
    )
}
