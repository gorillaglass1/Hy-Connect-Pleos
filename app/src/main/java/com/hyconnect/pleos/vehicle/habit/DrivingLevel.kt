package com.hyconnect.pleos.vehicle.habit

/**
 * 누적 주행 포인트로 도달한 운전자 레벨.
 *
 * 레벨은 [POINTS_PER_LEVEL] 단위로 한 단계씩 오른다(레벨 1은 0P부터 시작).
 * UI는 [pointsIntoLevel]/[pointsForNextLevel]로 다음 레벨까지의 진행률 바를 그린다.
 */
data class DrivingLevel(
    val level: Int,
    val title: String,
    // 현재 레벨 구간에서 모은 포인트(0 ~ pointsForNextLevel).
    val pointsIntoLevel: Long,
    // 다음 레벨로 올라가는 데 필요한 한 구간의 포인트.
    val pointsForNextLevel: Long,
) {
    /** 다음 레벨까지 남은 포인트. */
    val pointsToNext: Long get() = pointsForNextLevel - pointsIntoLevel

    /** 현재 레벨 구간 진행률(0f~1f). 진행 바에 그대로 쓴다. */
    val progress: Float
        get() = if (pointsForNextLevel > 0) {
            (pointsIntoLevel.toFloat() / pointsForNextLevel).coerceIn(0f, 1f)
        } else {
            0f
        }

    companion object {
        /** 한 레벨을 올리는 데 필요한 누적 포인트. */
        const val POINTS_PER_LEVEL = 1_000L

        // 레벨대별 칭호. 레벨이 표 범위를 넘으면 마지막 칭호를 유지한다.
        private val TITLES = listOf(
            "새내기 드라이버",   // Lv.1
            "주행 입문자",        // Lv.2
            "안정 주행러",        // Lv.3
            "베테랑 드라이버",    // Lv.4
            "마스터 드라이버",    // Lv.5
            "에코 드라이빙 마스터", // Lv.6+
        )

        /** 누적 포인트로부터 현재 레벨 정보를 만든다. */
        fun forPoints(totalPoints: Long): DrivingLevel {
            val safeTotal = totalPoints.coerceAtLeast(0)
            val level = (safeTotal / POINTS_PER_LEVEL).toInt() + 1
            val pointsIntoLevel = safeTotal % POINTS_PER_LEVEL
            val title = TITLES.getOrElse(level - 1) { TITLES.last() }
            return DrivingLevel(
                level = level,
                title = title,
                pointsIntoLevel = pointsIntoLevel,
                pointsForNextLevel = POINTS_PER_LEVEL,
            )
        }
    }
}
