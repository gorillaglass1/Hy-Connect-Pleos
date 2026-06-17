package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.network.NlRecommendedStationDto
import com.hyconnect.pleos.data.network.NlStationRecommendationResponseDto

fun NlStationRecommendationResponseDto.toStationRecommendation(): StationRecommendation =
    StationRecommendation(
        driverMessage = messageForDriver,
        stations = recommendedStations.mapIndexed { index, dto ->
            // 서버가 is_recommended를 채워주지 않으면 첫 번째 후보를 추천으로 본다.
            dto.toHydrogenStation(isRecommended = dto.isRecommended || index == 0)
        },
    )

fun NlRecommendedStationDto.toHydrogenStation(isRecommended: Boolean): HydrogenStation =
    HydrogenStation(
        id = hydrogenStationId.toString(),
        name = name,
        address = address,
        status = stationStatus ?: "상태 정보 없음",
        pressureInfo = pressureInfo ?: "압력 정보 없음",
        distanceKm = distanceKm ?: detourKm ?: 0.0,
        waitMinutes = waitMinutes ?: 0,
        isRecommended = isRecommended,
        latitude = latitude,
        longitude = longitude,
    )
