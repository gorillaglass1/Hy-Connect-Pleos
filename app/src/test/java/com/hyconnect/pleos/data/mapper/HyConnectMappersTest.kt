package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.network.DeliveryStationDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HyConnectMappersTest {
    @Test
    fun deliveryStationMapsToHydrogenStation() {
        val station = deliveryDto(isRecommended = true).toHydrogenStation()

        assertEquals("2820020121HS2019018", station.id)
        assertEquals("H인천수소충전소 에코스테이션", station.name)
        assertEquals("인천 남동구 청능대로468번길 1", station.address)
        assertEquals("운영중", station.status)
        assertEquals("700bar 사용 가능", station.pressureInfo)
        assertEquals(175.8, station.distanceKm, 0.0)
        assertEquals(26, station.waitMinutes)
        assertTrue(station.isRecommended)
        assertEquals(37.39867905, station.latitude ?: 0.0, 0.0)
        assertEquals(126.71148794, station.longitude ?: 0.0, 0.0)
    }

    @Test
    fun nullableFieldsFallBackToSafeDefaults() {
        val station = DeliveryStationDto(
            id = "id-1",
            name = "이름",
            address = null,
            status = null,
            pressureInfo = null,
            distanceKm = null,
            waitMinutes = null,
            isRecommended = false,
            latitude = null,
            longitude = null,
        ).toHydrogenStation()

        assertEquals("", station.address)
        assertEquals("", station.status)
        assertEquals("", station.pressureInfo)
        assertEquals(0.0, station.distanceKm, 0.0)
        assertEquals(0, station.waitMinutes)
        assertFalse(station.isRecommended)
        assertNull(station.latitude)
        assertNull(station.longitude)
    }

    @Test
    fun deliveryListHighlightsRecommendedStationInDriverMessage() {
        val recommendation = listOf(
            deliveryDto(id = "A", name = "A충전소", waitMinutes = 5, isRecommended = false),
            deliveryDto(id = "B", name = "B충전소", waitMinutes = 12, isRecommended = true),
        ).toStationRecommendationFromDelivery()

        assertEquals(2, recommendation.stations.size)
        // isRecommended=true 인 B충전소가 배너 문구의 기준이 된다.
        val message = recommendation.driverMessage
        assertTrue(message != null && message.contains("B충전소"))
        assertTrue(message!!.contains("12분"))
    }

    @Test
    fun deliveryListFallsBackToFirstStationWhenNoneRecommended() {
        val recommendation = listOf(
            deliveryDto(id = "A", name = "A충전소", waitMinutes = 7, isRecommended = false),
            deliveryDto(id = "B", name = "B충전소", waitMinutes = 3, isRecommended = false),
        ).toStationRecommendationFromDelivery()

        val message = recommendation.driverMessage
        assertTrue(message != null && message.contains("A충전소"))
    }

    @Test
    fun emptyDeliveryListProducesNullDriverMessage() {
        val recommendation = emptyList<DeliveryStationDto>().toStationRecommendationFromDelivery()

        assertNull(recommendation.driverMessage)
        assertTrue(recommendation.stations.isEmpty())
    }

    private fun deliveryDto(
        id: String = "2820020121HS2019018",
        name: String = "H인천수소충전소 에코스테이션",
        waitMinutes: Int? = 26,
        isRecommended: Boolean = false,
    ) = DeliveryStationDto(
        id = id,
        name = name,
        address = "인천 남동구 청능대로468번길 1",
        status = "운영중",
        pressureInfo = "700bar 사용 가능",
        distanceKm = 175.8,
        waitMinutes = waitMinutes,
        isRecommended = isRecommended,
        latitude = 37.39867905,
        longitude = 126.71148794,
    )
}
