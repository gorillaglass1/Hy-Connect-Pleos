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
    fun getNlRecommendedStationsMapsPersonalizedResponse() = runBlocking {
        enqueueJson(personalizedJson)

        val result = repository.getNlRecommendedStations(
            nlQuery = "인천에 있고 대기 차량이 적은 충전소",
            remainingRange = 45,
        )

        assertTrue(result is NetworkResult.Success)
        val recommendation = (result as NetworkResult.Success).data
        assertEquals(1, recommendation.stations.size)
        val station = recommendation.stations.first()
        assertEquals("2820020121HS2019018", station.id)
        assertEquals("H인천수소충전소 에코스테이션", station.name)
        assertTrue(station.isRecommended)
        assertEquals(37.39867905, station.latitude ?: 0.0, 0.0)
        assertEquals(126.71148794, station.longitude ?: 0.0, 0.0)

        val request = server.takeRequest()
        assertEquals("/recommendations/personalized", request.path)
        assertTrue(request.body.readUtf8().contains("nl_query"))
    }

    @Test
    fun submitStationSelectionPostsChrstnMnoToLearnEndpoint() = runBlocking {
        enqueueJson(preferenceJson)

        val result = repository.submitStationSelection(chrstnMno = "2820020121HS2019018")

        assertTrue(result is NetworkResult.Success)
        val preference = (result as NetworkResult.Success).data
        assertEquals(1, preference.userId)
        assertEquals("0.97", preference.weightPrice)

        val request = server.takeRequest()
        assertEquals("/users/1/preferences/learn", request.path)
        assertTrue(request.body.readUtf8().contains("2820020121HS2019018"))
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

private val personalizedJson = """
[
  {
    "chrstn_mno": "2820020121HS2019018",
    "chrstn_nm": "H인천수소충전소 에코스테이션",
    "road_nm_addr": "인천 남동구 청능대로468번길 1",
    "vhcle_knd_cd": null,
    "vhcle_knd_nm": null,
    "ntsl_pc": 11000,
    "distance_to_station": 175.82,
    "distance_to_destination": 12.4,
    "detour_distance": 0.36,
    "wait_vehicles": 0,
    "wait_time_minutes": 0,
    "facilities": ["대기실", "세차장", "편의점", "화장실"],
    "is_reachable": true,
    "sub_scores": { "price": 70.0, "waiting_time": 100.0, "distance": 95.0, "facilities": 60.0 },
    "final_score": 84.1,
    "recommendation_reason": "사용자 가중치 분석 결과 전반적 매칭도가 매우 높습니다.",
    "delivery_payload": {
      "chrstn_mno": "2820020121HS2019018",
      "chrstn_nm": "H인천수소충전소 에코스테이션",
      "road_nm_addr": "인천 남동구 청능대로468번길 1",
      "latitude": 37.39867905,
      "longitude": 126.71148794,
      "vhcle_knd_cd": null,
      "vhcle_knd_nm": null,
      "ntsl_pc": 11000,
      "distance_to_station": 175.82,
      "detour_distance": 0.36,
      "wait_vehicles": 0,
      "wait_time_minutes": 0,
      "facilities": ["대기실", "세차장", "편의점", "화장실"],
      "is_reachable": true,
      "final_score": 84.1,
      "recommendation_reason": "사용자 가중치 분석 결과 전반적 매칭도가 매우 높습니다."
    }
  }
]
""".trimIndent()

private val preferenceJson = """
{
  "user_id": 1,
  "weight_price": "0.97",
  "weight_waiting_time": "1.05",
  "weight_distance": "1.04",
  "weight_facilities": "0.94",
  "safety_margin": "1.10",
  "created_at": "2026-05-08T00:00:00Z",
  "updated_at": "2026-05-08T00:00:00Z"
}
""".trimIndent()
