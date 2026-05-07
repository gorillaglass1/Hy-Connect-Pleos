package com.hyconnect.pleos.data.model

data class AiRecommendation(
    val label: String = "AI 추천",
    val title: String = "지금 충전하기 좋은 타이밍이에요",
    val dustSummary: String = "현재 미세먼지 21µg/m³ (좋음)이며,",
    val routeSummary: String = "현재 경로도 대기없이 빠르게 갈 수 있어요.",
)
