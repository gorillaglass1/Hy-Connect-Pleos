package com.hyconnect.pleos.vehicle.habit

/**
 * 통산 운전습관 프로파일. [DrivingHabitStore]가 로컬(DataStore)에 영속하며,
 * 주행 세션([DrivingSession])이 끝날 때마다 누적된다.
 *
 * 샘플앱(ai.pleos...handson.vehicle)의 안전운전 분석 규칙을 이식하되,
 * 여기서는 "안전운전 점수"가 목적이 아니라 개인화 충전 추천의 입력 신호로 쓴다.
 * (예: 급가속/급정거가 잦으면 연료 소모가 빨라 더 여유 있는 충전 타이밍을 추천)
 */
data class DrivingHabitProfile(
    val totalSessions: Int = 0,
    val totalDrivingMinutes: Int = 0,
    val harshAccelCount: Int = 0,
    val harshBrakeCount: Int = 0,
    val incautiousCount: Int = 0,
    // 세션 종료 점수의 누적합. 평균 점수 산출에 쓴다.
    val cumulativeScore: Long = 0,
    val lastUpdatedEpochMs: Long = 0,
) {
    val totalEvents: Int get() = harshAccelCount + harshBrakeCount + incautiousCount

    /** 세션 평균 점수(0~100). 기록이 없으면 만점 기준값을 반환한다. */
    val avgScore: Int
        get() = if (totalSessions > 0) {
            (cumulativeScore / totalSessions).toInt()
        } else {
            DrivingHabitAnalyzer.BASE_SCORE
        }

    /** 주행 1시간당 위험 이벤트 빈도. 주행 시간이 0이면 0. */
    val eventsPerHour: Double
        get() = if (totalDrivingMinutes > 0) totalEvents * 60.0 / totalDrivingMinutes else 0.0

    /** 누적 기록 존재 여부. 서버 요청에 운전습관을 실을지 판단할 때 쓴다. */
    val hasData: Boolean get() = totalSessions > 0

    /** 평균 점수 기준 주행 성향 분류. Gemini 프롬프트의 핵심 라벨이 된다. */
    val style: DrivingStyle
        get() = when {
            !hasData -> DrivingStyle.UNKNOWN
            avgScore >= CALM_SCORE_THRESHOLD -> DrivingStyle.CALM
            avgScore >= MODERATE_SCORE_THRESHOLD -> DrivingStyle.MODERATE
            else -> DrivingStyle.AGGRESSIVE
        }

    /** 한 세션 결과를 누적해 새 프로파일을 만든다(불변 갱신). */
    fun accumulate(session: DrivingSession, nowMs: Long): DrivingHabitProfile = copy(
        totalSessions = totalSessions + 1,
        totalDrivingMinutes = totalDrivingMinutes + session.durationMinutes,
        harshAccelCount = harshAccelCount + session.harshAccelCount,
        harshBrakeCount = harshBrakeCount + session.harshBrakeCount,
        incautiousCount = incautiousCount + session.incautiousCount,
        cumulativeScore = cumulativeScore + session.score,
        lastUpdatedEpochMs = nowMs,
    )

    companion object {
        const val CALM_SCORE_THRESHOLD = 90
        const val MODERATE_SCORE_THRESHOLD = 70
    }
}

/** 주행 성향. 서버/Gemini로 보낼 때는 [wire] 소문자 문자열을 쓴다. */
enum class DrivingStyle {
    CALM,
    MODERATE,
    AGGRESSIVE,
    UNKNOWN;

    val wire: String get() = name.lowercase()
}
