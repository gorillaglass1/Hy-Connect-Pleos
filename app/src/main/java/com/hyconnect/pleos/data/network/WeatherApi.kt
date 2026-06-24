package com.hyconnect.pleos.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * FastAPI 날씨 API 계약. 경로는 BASE_URL 기준 상대 경로다.
 */
interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): WeatherResponse
}
