package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * FastAPI 서버 `GET /weather` 응답 모델.
 *
 * sky, precipitationType 는 서버에서 이미 한글 문자열("구름많음", "없음" 등)로
 * 변환되어 내려오므로 프론트에서 별도 매핑하지 않는다.
 */
data class WeatherResponse(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("sky") val sky: String,
    @SerializedName("precipitation_type") val precipitationType: String,
    @SerializedName("humidity") val humidity: Double,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("base_time") val baseTime: String,
    @SerializedName("nx") val nx: Int,
    @SerializedName("ny") val ny: Int,
)
