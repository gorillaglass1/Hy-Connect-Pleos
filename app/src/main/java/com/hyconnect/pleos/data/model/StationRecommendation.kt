package com.hyconnect.pleos.data.model

/**
 * 자연어 추천 응답을 Compose UI에서 쓰기 좋은 형태로 정리한 모델.
 * - driverMessage: 운전자에게 보여줄 한 줄 안내(연료 부족 배너 문구로 사용)
 * - stations: 추천 충전소 목록(거리순/추천순으로 서버가 정렬해 내려준다고 가정)
 */
data class StationRecommendation(
    val driverMessage: String?,
    val stations: List<HydrogenStation>,
)
