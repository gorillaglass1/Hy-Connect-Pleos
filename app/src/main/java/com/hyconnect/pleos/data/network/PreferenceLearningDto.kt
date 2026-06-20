package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

/**
 * 선택 기반 선호도 학습 요청(`POST /users/{user_id}/preferences/learn`).
 * 프론트는 선택한 충전소 관리번호만 보낸다. 점수는 서버가 추천 시 저장한 스냅샷으로 처리한다.
 */
data class PreferenceLearningRequestDto(
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
)

/**
 * 사용자 선호 가중치 응답(`UserPreferenceResponse`).
 * 서버가 가중치/소수값을 문자열 Decimal("0.97")로 직렬화하므로 String으로 받고 필요 시 [toDouble]한다.
 */
data class UserPreferenceResponseDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("weight_price")
    val weightPrice: String?,
    @SerializedName("weight_waiting_time")
    val weightWaitingTime: String?,
    @SerializedName("weight_distance")
    val weightDistance: String?,
    @SerializedName("weight_facilities")
    val weightFacilities: String?,
    @SerializedName("safety_margin")
    val safetyMargin: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
)
