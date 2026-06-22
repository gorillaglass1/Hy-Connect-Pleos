package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 개인화 추천 요청 페이로드. 서버 `POST /recommendations/personalized`(및 delivery-payloads)와 1:1 대응.
 *
 * - 충전소 식별자/경로범위/반경은 서버가 내부적으로 계산하므로 클라이언트가 보내지 않는다.
 * - [nlQuery]는 선택 필드다. 자연어 검색은 별도 엔드포인트가 아니라 이 필드로 전달한다(없으면 null → 직렬화 생략).
 * - [userId]는 로그인 사용자가 없으면 null로 둔다. Gson 기본 설정상 null 필드는 직렬화에서 빠지므로
 *   "유저 정보 없이 비워서" 요청이 전송된다.
 */
data class PersonalizedRecommendationRequestDto(
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("current_latitude")
    val currentLatitude: Double,
    @SerializedName("current_longitude")
    val currentLongitude: Double,
    // 목적지가 없으면(경로 안내 중이 아니면) 두 값 모두 null로 두어 본문에서 생략한다.
    // 서버는 위/경도를 모두 생략하면 현재 위치 근처 충전소를 추천한다. (한쪽만 보내면 거부)
    @SerializedName("destination_latitude")
    val destinationLatitude: Double? = null,
    @SerializedName("destination_longitude")
    val destinationLongitude: Double? = null,
    @SerializedName("remaining_range")
    val remainingRange: Double,
    @SerializedName("nl_query")
    val nlQuery: String? = null,
)

/**
 * 개인화 추천 응답 항목(`RecommendedStationResponse`). 점수순으로 정렬되어 내려온다.
 * 충전소 식별자는 정수가 아니라 문자열 [chrstnMno]다.
 */
data class RecommendedStationResponseDto(
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
    @SerializedName("chrstn_nm")
    val chrstnNm: String,
    @SerializedName("road_nm_addr")
    val roadNmAddr: String?,
    @SerializedName("vhcle_knd_cd")
    val vhcleKndCd: String?,
    @SerializedName("vhcle_knd_nm")
    val vhcleKndNm: String?,
    @SerializedName("ntsl_pc")
    val ntslPc: Int?,
    @SerializedName("distance_to_station")
    val distanceToStation: Double?,
    @SerializedName("distance_to_destination")
    val distanceToDestination: Double?,
    @SerializedName("detour_distance")
    val detourDistance: Double?,
    @SerializedName("wait_vehicles")
    val waitVehicles: Int?,
    @SerializedName("wait_time_minutes")
    val waitTimeMinutes: Int?,
    @SerializedName("facilities")
    val facilities: List<String> = emptyList(),
    @SerializedName("is_reachable")
    val isReachable: Boolean = true,
    @SerializedName("sub_scores")
    val subScores: SubScoresDto?,
    @SerializedName("final_score")
    val finalScore: Double?,
    @SerializedName("recommendation_reason")
    val recommendationReason: String?,
    @SerializedName("delivery_payload")
    val deliveryPayload: RecommendationDeliveryPayloadDto?,
)

/**
 * `POST /recommendations/personalized/delivery-payloads` 응답 항목.
 *
 * 이 엔드포인트는 차량/화면에 바로 쓸 수 있도록 이미 정제된 camelCase 필드를 내려준다.
 * (점수순으로 정렬되어 있고, [isRecommended]가 true인 1위 항목이 대표 추천이다.)
 * UI 모델 [com.hyconnect.pleos.data.model.HydrogenStation]과 필드 구성이 거의 같지만,
 * 네트워크 계약과 UI 모델을 분리해 두기 위해 별도 DTO로 받는다.
 */
data class DeliveryStationDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("address")
    val address: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("pressureInfo")
    val pressureInfo: String?,
    @SerializedName("distanceKm")
    val distanceKm: Double?,
    @SerializedName("waitMinutes")
    val waitMinutes: Int?,
    @SerializedName("isRecommended")
    val isRecommended: Boolean = false,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
)

data class SubScoresDto(
    @SerializedName("price")
    val price: Double?,
    @SerializedName("waiting_time")
    val waitingTime: Double?,
    @SerializedName("distance")
    val distance: Double?,
    @SerializedName("facilities")
    val facilities: Double?,
)

/**
 * 차량 전송용 flat 페이로드(`RecommendationDeliveryPayload`).
 * 중첩/sub_scores/딥링크 없이 좌표([latitude]/[longitude])를 포함한다. 지도/내비 좌표는 이 값을 쓴다.
 */
data class RecommendationDeliveryPayloadDto(
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
    @SerializedName("chrstn_nm")
    val chrstnNm: String,
    @SerializedName("road_nm_addr")
    val roadNmAddr: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("vhcle_knd_cd")
    val vhcleKndCd: String?,
    @SerializedName("vhcle_knd_nm")
    val vhcleKndNm: String?,
    @SerializedName("ntsl_pc")
    val ntslPc: Int?,
    @SerializedName("distance_to_station")
    val distanceToStation: Double?,
    @SerializedName("detour_distance")
    val detourDistance: Double?,
    @SerializedName("wait_vehicles")
    val waitVehicles: Int?,
    @SerializedName("wait_time_minutes")
    val waitTimeMinutes: Int?,
    @SerializedName("facilities")
    val facilities: List<String> = emptyList(),
    @SerializedName("is_reachable")
    val isReachable: Boolean = true,
    @SerializedName("final_score")
    val finalScore: Double?,
    @SerializedName("recommendation_reason")
    val recommendationReason: String?,
)
