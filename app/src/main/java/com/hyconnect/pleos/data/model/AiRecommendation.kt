package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class AiRecommendation(
    // 서버 추천 기록과 충전소 정보를 합친 Compose UI 표시용 모델이다.
    @SerializedName("title")
    val title: String = "추천 정보를 불러오는 중입니다.",
    @SerializedName("dust_summary")
    val dustSummary: String = "",
    @SerializedName("route_summary")
    val routeSummary: String = "",
    @SerializedName("reason")
    val reason: String = "",
)
