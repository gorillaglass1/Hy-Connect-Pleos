package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.network.HydrogenChargerDto
import com.hyconnect.pleos.data.network.HydrogenStationDto
import com.hyconnect.pleos.data.network.HydrogenStationRealtimeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HyConnectMappersTest {
    @Test
    fun mergeWithRealtimeAndChargersMapsStationUiModel() {
        val stations = listOf(stationDto())
        val realtime = listOf(realtimeDto(avgWaitTime = 7, queueCount = 3))
        val chargers = listOf(chargerDto(hydrogenPressureBar = 700, pressureType = "고압"))

        val result = stations.mergeWithRealtimeAndChargers(
            realtimeList = realtime,
            chargerList = chargers,
            recommendationHistories = emptyList(),
            currentLat = 37.5665,
            currentLng = 126.9780,
        )

        assertEquals("1", result.first().id)
        assertEquals("서울 수소충전소", result.first().name)
        assertEquals("운영 중", result.first().status)
        assertEquals("700bar 사용 가능", result.first().pressureInfo)
        assertEquals(7, result.first().waitMinutes)
        assertTrue(result.first().isRecommended)
    }

    @Test
    fun pressureInfoDetects700BarFromPressureType() {
        val result = listOf(stationDto()).mergeWithRealtimeAndChargers(
            realtimeList = listOf(realtimeDto(avgWaitTime = 5, queueCount = 1)),
            chargerList = listOf(chargerDto(hydrogenPressureBar = null, pressureType = "350/700bar")),
            recommendationHistories = emptyList(),
            currentLat = null,
            currentLng = null,
        )

        assertEquals("700bar 사용 가능", result.first().pressureInfo)
    }

    @Test
    fun waitMinutesFallsBackToQueueCountTimesFive() {
        val result = listOf(stationDto()).mergeWithRealtimeAndChargers(
            realtimeList = listOf(realtimeDto(avgWaitTime = null, queueCount = 4)),
            chargerList = listOf(chargerDto(hydrogenPressureBar = null, pressureType = "350bar")),
            recommendationHistories = emptyList(),
            currentLat = null,
            currentLng = null,
        )

        assertEquals(20, result.first().waitMinutes)
    }

    private fun stationDto() = HydrogenStationDto(
        hydrogenStationId = 1,
        name = "서울 수소충전소",
        address = "서울 중구",
        latitude = 37.5665,
        longitude = 126.9780,
        contactNumber = null,
        startTime = "09:00",
        endTime = "18:00",
        totalChargers = 2,
        paymentSupported = "card",
    )

    private fun realtimeDto(
        avgWaitTime: Int?,
        queueCount: Int,
    ) = HydrogenStationRealtimeDto(
        realtimeId = 1,
        hydrogenStationId = 1,
        availableChargers = 1,
        inUseChargers = 1,
        queueCount = queueCount,
        avgWaitTime = avgWaitTime,
        hydrogenStockKg = 120.0,
        stationStatus = "운영 중",
        lastRestockAt = null,
        nextRestockSchedule = null,
        utilizationRate = 0.5,
        updatedAt = "2026-05-08T00:00:00Z",
    )

    private fun chargerDto(
        hydrogenPressureBar: Int?,
        pressureType: String,
    ) = HydrogenChargerDto(
        hydrogenChargerId = 100,
        hydrogenStationId = 1,
        chargerStatus = "available",
        chargerType = "fast",
        hydrogenPressureBar = hydrogenPressureBar,
        pressureType = pressureType,
        restockSchedule = null,
    )
}
