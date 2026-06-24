package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.network.DeliveryStationDto

/**
 * `delivery-payloads` 응답을 Compose UI 모델로 변환한다.
 * 서버가 이미 camelCase로 정제해 내려준 값을 그대로 쓰고, 누락 필드는 안전한 기본값으로 채운다.
 */
fun DeliveryStationDto.toHydrogenStation(): HydrogenStation =
    HydrogenStation(
        id = id,
        name = name,
        address = address.orEmpty(),
        status = status.orEmpty(),
        pressureInfo = pressureInfo.orEmpty(),
        // 서버가 이미 정제해 내려준 거리값을 그대로 쓴다. (추가 반올림하지 않음)
        distanceKm = distanceKm ?: 0.0,
        waitMinutes = waitMinutes ?: 0,
        isRecommended = isRecommended,
        latitude = latitude,
        longitude = longitude,
    )

/**
 * `delivery-payloads` 응답 리스트를 [StationRecommendation]으로 변환한다.
 * 추천(isRecommended=true) 항목, 없으면 1위 항목 기준으로 운전자 배너 문구를 만든다.
 */
fun List<DeliveryStationDto>.toStationRecommendationFromDelivery(): StationRecommendation {
    val stations = map { it.toHydrogenStation() }
    val highlight = stations.firstOrNull { it.isRecommended } ?: stations.firstOrNull()
    return StationRecommendation(
        driverMessage = highlight?.let { "${it.name}을(를) 추천해요. 대기 약 ${it.waitMinutes}분." },
        stations = stations,
    )
}
