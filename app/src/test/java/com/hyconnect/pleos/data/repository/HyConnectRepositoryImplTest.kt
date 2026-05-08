package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HyConnectRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: HyConnectRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HyConnectService::class.java)

        repository = HyConnectRepositoryImpl(service = service)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getRecommendedStationsUsesOptimizedRecommendationResponse() = runBlocking {
        enqueueJson(vehicleJson)
        enqueueJson(stationsJson)
        enqueueJson(realtimeJson)
        enqueueJson(chargersJson)
        enqueueJson(optimizedRecommendationJson)

        val result = repository.getRecommendedStations()

        assertTrue(result is NetworkResult.Success)
        val stations = (result as NetworkResult.Success).data
        assertEquals(1, stations.size)
        assertEquals("데모 강동 수소스테이션", stations.first().name)
        assertEquals("700bar 사용 가능", stations.first().pressureInfo)
        assertEquals(5, stations.first().waitMinutes)
        assertEquals(37.5301, stations.first().latitude ?: 0.0, 0.0)
        assertEquals(127.1238, stations.first().longitude ?: 0.0, 0.0)
        assertTrue(stations.first().isRecommended)
    }

    private fun enqueueJson(body: String) {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body),
        )
    }
}

private val vehicleJson = """
{
  "vehicle_id": 1,
  "user_id": 1,
  "vehicle_number": "12가3456",
  "model": "NEXO",
  "vehicle_type": "SUV",
  "fuel_type": "hydrogen",
  "tank_capacity": 6.33,
  "avg_efficiency": 96.0,
  "registered_at": "2026-05-08T00:00:00Z"
}
""".trimIndent()

private val stationsJson = """
[
  {
    "hydrogen_station_id": 1,
    "name": "서울 수소충전소",
    "address": "서울 중구",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "contact_number": null,
    "start_time": "09:00",
    "end_time": "18:00",
    "total_chargers": 2,
    "payment_supported": "card"
  }
]
""".trimIndent()

private val realtimeJson = """
[
  {
    "realtime_id": 10,
    "hydrogen_station_id": 1,
    "available_chargers": 1,
    "in_use_chargers": 1,
    "queue_count": 2,
    "avg_wait_time": null,
    "hydrogen_stock_kg": 120.0,
    "station_status": "운영 중",
    "last_restock_at": null,
    "next_restock_schedule": null,
    "utilization_rate": 0.5,
    "updated_at": "2026-05-08T00:00:00Z"
  }
]
""".trimIndent()

private val chargersJson = """
[
  {
    "hydrogen_charger_id": 100,
    "hydrogen_station_id": 1,
    "charger_status": "available",
    "charger_type": "fast",
    "hydrogen_pressure_bar": 700,
    "pressure_type": "700bar",
    "restock_schedule": null
  }
]
""".trimIndent()

private val optimizedRecommendationJson = """
{
  "recommendation_id": 1778221628174,
  "recommended_station": {
    "hydrogen_station_id": 901,
    "name": "데모 강동 수소스테이션",
    "address": "서울 강동구 데모로 12",
    "latitude": 37.5301,
    "longitude": 127.1238,
    "selected_charger_id": 9001
  },
  "score": 90.7,
  "reason": "현재 주행가능거리로 도달 가능하며 700bar 충전을 지원합니다.",
  "decision_factors": {
    "reachable": true,
    "estimated_arrival_range_km": 82,
    "detour_distance_km": 4.5,
    "estimated_wait_time_min": 5,
    "price_per_kg": 8800,
    "supports_700bar": true,
    "station_status": "OPEN"
  },
  "recommendations": [
    {
      "rank": 1,
      "hydrogen_station_id": 901,
      "name": "데모 강동 수소스테이션",
      "address": "서울 강동구 데모로 12",
      "latitude": 37.5301,
      "longitude": 127.1238,
      "selected_charger_id": 9001,
      "score": 90.7,
      "reason": "현재 주행가능거리로 도달 가능하며 700bar 충전을 지원합니다.",
      "highlight": "대기 시간이 짧음",
      "decision_factors": {
        "reachable": true,
        "estimated_arrival_range_km": 82,
        "detour_distance_km": 4.5,
        "estimated_wait_time_min": 5,
        "price_per_kg": 8800,
        "supports_700bar": true,
        "station_status": "OPEN"
      }
    }
  ],
  "alternatives": [],
  "message_for_driver": "약 14.2km 전방 데모 강동 수소스테이션을 추천합니다. 예상 대기 시간은 5분입니다.",
  "created_at": "2026-05-08T06:27:08.174230Z"
}
""".trimIndent()
