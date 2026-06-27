package com.hyconnect.pleos.data.mapper

import com.google.gson.Gson
import com.hyconnect.pleos.data.model.DashboardActionStyle
import com.hyconnect.pleos.data.model.DashboardActionType
import com.hyconnect.pleos.data.model.FuelStatus
import com.hyconnect.pleos.data.model.MetricTone
import com.hyconnect.pleos.data.network.SufficientDashboardDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SufficientDashboardMapperTest {
    private val gson = Gson()

    /** 서버 `POST /nearest-recommendation` 실제 응답(snake_case, 위도 키 `let`)을 그대로 검증한다. */
    @Test
    fun nearestRecommendationResponseMapsToDashboard() {
        val dto = gson.fromJson(serverResponseJson, SufficientDashboardDto::class.java)
        val dashboard = dto.toSufficientDashboard()

        // vehicle: remaining_range(Double) → rangeKm(Int)
        assertEquals("hydrogen", dashboard.vehicle.fuelType)
        assertEquals(100, dashboard.vehicle.fuelPercent)
        assertEquals(500, dashboard.vehicle.rangeKm)

        // ai_insight: status 문자열 → enum, metrics 매핑
        assertEquals(FuelStatus.SUFFICIENT, dashboard.aiInsight.status)
        assertEquals("충분", dashboard.aiInsight.statusLabel)
        assertEquals("잔량 충분", dashboard.aiInsight.subtitle)
        assertNull(dashboard.aiInsight.updatedAt)
        assertEquals(3, dashboard.aiInsight.metrics.size)
        val rangeMetric = dashboard.aiInsight.metrics.first()
        assertEquals("주행가능거리", rangeMetric.label)
        assertEquals("500.0", rangeMetric.value)
        assertEquals("km", rangeMetric.unit)
        // 응답에 tone이 없으므로 항상 NEUTRAL
        assertEquals(MetricTone.NEUTRAL, rangeMetric.tone)
        // unit이 null이면 unit은 null로 유지된다
        assertNull(dashboard.aiInsight.metrics[1].unit)

        // recommended_station: 키 매핑(chrstn_mno/road_nm_addr/ntsl_pc/wait_vhcle_alge/let/lon)
        val station = dashboard.recommendedStation
        assertNotNull(station)
        requireNotNull(station)
        assertEquals("1156020121HS2019014", station.stationId)
        assertEquals("H국회수소충전소", station.name)
        assertEquals("서울 영등포구 의사당대로 1", station.address)
        assertEquals(6.97, station.distanceKm, 0.0)
        assertEquals(9900, station.pricePerKg)
        // price_diff_from_avg 는 Double(0.0) → Int 반올림
        assertEquals(0, station.priceDiffFromAvg)
        assertEquals(0, station.estimatedCost)
        assertEquals(4, station.waitingVehicles)
        assertTrue(station.isOpen)
        // 위도 키는 `let`
        assertEquals(37.52820123884357, station.latitude ?: 0.0, 0.0)
        assertEquals(126.9150871038437, station.longitude ?: 0.0, 0.0)
        // 응답에 없는 필드는 기본값
        assertNull(station.badge)
        assertFalse(station.realtimePrice)
        assertEquals(0, station.etaMinutes)
        assertTrue(station.available)
    }

    /** 응답엔 actions가 없으므로, 추천소가 있으면 매퍼가 기본 버튼(경로안내)을 생성한다. */
    @Test
    fun actionsAreSynthesizedWhenStationPresent() {
        val dto = gson.fromJson(serverResponseJson, SufficientDashboardDto::class.java)
        val actions = dto.toSufficientDashboard().actions

        assertEquals(1, actions.size)
        assertEquals(DashboardActionType.NAVIGATE, actions[0].type)
        assertEquals(DashboardActionStyle.PRIMARY, actions[0].style)
        // NAVIGATE 버튼은 추천소 식별자(chrstn_mno)를 들고 있어야 한다.
        assertEquals("1156020121HS2019014", actions[0].stationId)
    }

    /** 서버가 뱃지를 붙여주면 그대로 유지한다(빈 문자열은 숨김). */
    @Test
    fun serverBadgeIsKept() {
        val withBadge = gson.fromJson(
            serverResponseJson.replace(
                "\"name\": \"H국회수소충전소\",",
                "\"name\": \"H국회수소충전소\",\n        \"badge\": \"근처 최저가\",",
            ),
            SufficientDashboardDto::class.java,
        )
        assertEquals("근처 최저가", withBadge.toSufficientDashboard().recommendedStation?.badge)

        val blankBadge = gson.fromJson(
            serverResponseJson.replace(
                "\"name\": \"H국회수소충전소\",",
                "\"name\": \"H국회수소충전소\",\n        \"badge\": \"   \",",
            ),
            SufficientDashboardDto::class.java,
        )
        assertNull(blankBadge.toSufficientDashboard().recommendedStation?.badge)
    }

    @Test
    fun noStationProducesNoActions() {
        val dto = gson.fromJson(
            """{ "screen": "battery_sufficient", "vehicle": null, "ai_insight": null, "recommended_station": null }""",
            SufficientDashboardDto::class.java,
        )
        val dashboard = dto.toSufficientDashboard()

        assertNull(dashboard.recommendedStation)
        assertTrue(dashboard.actions.isEmpty())
        // null vehicle/ai_insight도 안전한 기본값으로 떨어진다
        assertEquals(0, dashboard.vehicle.rangeKm)
        assertEquals(FuelStatus.UNKNOWN, dashboard.aiInsight.status)
    }

    private val serverResponseJson = """
    {
        "screen": "battery_sufficient",
        "vehicle": {
            "fuel_percent": 100,
            "remaining_range": 500.0,
            "fuel_type": "hydrogen"
        },
        "ai_insight": {
            "status": "sufficient",
            "status_label": "충분",
            "subtitle": "잔량 충분",
            "message": "잔량이 넉넉해요. 약 500.0km 더 주행할 수 있어요.",
            "metrics": [
                { "label": "주행가능거리", "value": "500.0", "unit": "km" },
                { "label": "권장 충전 시점", "value": "여유 있음", "unit": null },
                { "label": "평균 소모율", "value": "정상", "unit": null }
            ]
        },
        "recommended_station": {
            "chrstn_mno": "1156020121HS2019014",
            "name": "H국회수소충전소",
            "road_nm_addr": "서울 영등포구 의사당대로 1",
            "distance_km": 6.97,
            "ntsl_pc": 9900,
            "price_diff_from_avg": 0.0,
            "estimated_cost": 0,
            "wait_vhcle_alge": 4,
            "is_open": true,
            "let": 37.52820123884357,
            "lon": 126.9150871038437
        }
    }
    """.trimIndent()
}
