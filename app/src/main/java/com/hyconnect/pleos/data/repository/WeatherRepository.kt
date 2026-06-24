package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.network.ApiClient
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.network.WeatherApi
import com.hyconnect.pleos.data.network.WeatherResponse
import com.hyconnect.pleos.data.network.safeApiCall

/**
 * 날씨 도메인 전용 Repository. 차량/충전소 도메인([HyConnectRepository])과 분리해서 둔다.
 */
interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): NetworkResult<WeatherResponse>
}

class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi = ApiClient.weatherApi,
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): NetworkResult<WeatherResponse> =
        safeApiCall { weatherApi.getWeather(lat, lon) }
}
