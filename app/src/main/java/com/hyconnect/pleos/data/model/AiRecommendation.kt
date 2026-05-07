package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class AiRecommendation(
    // TODO: 서버 /recommendation 응답 스키마가 확정되면 필드명과 타입을 실제 문서에 맞춘다.
    @SerializedName("label")
    val label: String = "AI 추천",
    @SerializedName("title")
    val title: String = "추천 정보를 불러오는 중입니다.",
    @SerializedName("dust_summary")
    val dustSummary: String = "",
    @SerializedName("route_summary")
    val routeSummary: String = "",
    @SerializedName("reason")
    val reason: String = "",
)
