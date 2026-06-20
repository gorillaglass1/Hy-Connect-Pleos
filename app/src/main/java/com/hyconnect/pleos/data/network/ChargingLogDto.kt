package com.hyconnect.pleos.data.network

import com.google.gson.annotations.SerializedName

// 충전소 식별자는 문자열 chrstn_mno. 서버에 차량 테이블이 없으므로 vehicle_id는 포함하지 않는다.
// TODO: 서버 charging_log_schema 원본 확인 후 필드명을 최종 정렬한다.
data class ChargingLogRequestDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("charged_amount")
    val chargedAmount: Double?,
    @SerializedName("charging_cost")
    val chargingCost: Double?,
    @SerializedName("waiting_time")
    val waitingTime: Int?,
)

data class ChargingLogResponseDto(
    @SerializedName("charging_log_id")
    val chargingLogId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("chrstn_mno")
    val chrstnMno: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("charged_amount")
    val chargedAmount: Double?,
    @SerializedName("charging_cost")
    val chargingCost: Double?,
    @SerializedName("waiting_time")
    val waitingTime: Int?,
    @SerializedName("created_at")
    val createdAt: String?,
)
