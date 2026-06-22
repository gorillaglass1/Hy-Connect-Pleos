package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.location.Destination
import com.hyconnect.pleos.location.DestinationStore
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        // DestinationStore는 전역 싱글톤이라 테스트 간 상태가 새지 않도록 비우고 시작한다.
        DestinationStore.clear()
    }

    @After
    fun tearDown() {
        server.shutdown()
        DestinationStore.clear()
    }

    @Test
    fun getNlRecommendedStationsMapsDeliveryResponse() = runBlocking {
        enqueueJson(deliveryJson)

        val result = repository.getNlRecommendedStations(
            nlQuery = "인천에 있고 대기 차량이 적은 충전소",
            remainingRange = 45,
        )

        assertTrue(result is NetworkResult.Success)
        val recommendation = (result as NetworkResult.Success).data
        assertEquals(2, recommendation.stations.size)
        val station = recommendation.stations.first()
        assertEquals("2811020121HS2021033", station.id)
        assertEquals("인천그린수소충전소", station.name)
        assertTrue(station.isRecommended)
        assertEquals(9.67, station.distanceKm, 0.0)
        assertEquals(26, station.waitMinutes)
        assertEquals(37.43866249, station.latitude ?: 0.0, 0.0)
        assertEquals(126.62001367, station.longitude ?: 0.0, 0.0)

        val request = server.takeRequest()
        assertEquals("/recommendations/personalized/delivery-payloads", request.path)
        // 로그인 사용자가 없으면 user_id는 본문에서 생략된다.
        val body = request.body.readUtf8()
        assertTrue(body.contains("nl_query"))
        assertTrue(!body.contains("user_id"))
    }

    @Test
    fun noDestinationOmitsDestinationCoordinatesFromRequest() = runBlocking {
        // 목적지가 없으면(경로 안내 중 아님) 목적지 좌표는 본문에서 아예 빠져야 한다. (0이 들어가면 안 됨)
        DestinationStore.clear()
        enqueueJson(deliveryJson)

        repository.getNlRecommendedStations(nlQuery = "가까운 충전소", remainingRange = 45)

        val body = server.takeRequest().body.readUtf8()
        assertFalse(body.contains("destination_latitude"))
        assertFalse(body.contains("destination_longitude"))
    }

    @Test
    fun emptyDestinationCoordinatesAreNotSentAsZero() = runBlocking {
        // 저장소에 (0,0) 빈 목적지가 남아 있어도 요청에 0이 새지 않아야 한다(요청 경계 방어).
        DestinationStore.update(Destination(latitude = 0.0, longitude = 0.0))
        enqueueJson(deliveryJson)

        repository.getNlRecommendedStations(nlQuery = "가까운 충전소", remainingRange = 45)

        val body = server.takeRequest().body.readUtf8()
        assertFalse(body.contains("destination_latitude"))
        assertFalse(body.contains("destination_longitude"))
    }

    @Test
    fun realDestinationCoordinatesAreSentInRequest() = runBlocking {
        // 실제 목적지가 있으면 좌표가 본문에 포함돼야 한다.
        DestinationStore.update(Destination(latitude = 37.5012, longitude = 127.0396))
        enqueueJson(deliveryJson)

        repository.getNlRecommendedStations(nlQuery = "경로상 충전소", remainingRange = 45)

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("destination_latitude"))
        assertTrue(body.contains("37.5012"))
        assertTrue(body.contains("destination_longitude"))
        assertTrue(body.contains("127.0396"))
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

private val deliveryJson = """
[
  {
    "id": "2811020121HS2021033",
    "name": "인천그린수소충전소",
    "address": "인천 중구 축항대로290번길 124",
    "status": "운영중",
    "pressureInfo": "700bar 사용 가능",
    "distanceKm": 9.67,
    "waitMinutes": 26,
    "isRecommended": true,
    "latitude": 37.43866249,
    "longitude": 126.62001367
  },
  {
    "id": "2811020121HS2025028",
    "name": "하이버스 인천신흥 수소충전소",
    "address": "인천 중구 서해대로94번길 57-33",
    "status": "운영중",
    "pressureInfo": "700bar 사용 가능",
    "distanceKm": 9.25,
    "waitMinutes": 33,
    "isRecommended": false,
    "latitude": 37.43658790,
    "longitude": 126.62406752
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
