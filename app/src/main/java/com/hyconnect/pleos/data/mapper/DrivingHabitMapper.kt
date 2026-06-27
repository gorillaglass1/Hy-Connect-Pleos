package com.hyconnect.pleos.data.mapper

import com.hyconnect.pleos.data.network.DrivingHabitDto
import com.hyconnect.pleos.vehicle.habit.DrivingHabitProfile
import kotlin.math.round

/**
 * 로컬 운전습관 프로파일을 서버 요청용 DTO로 변환한다.
 * 서버는 이 값을 Gemini 프롬프트에 합성해 개인화 충전 인사이트를 생성한다.
 * (eventsPerHour는 소수 둘째 자리까지만 보낸다.)
 */
fun DrivingHabitProfile.toDrivingHabitDto(): DrivingHabitDto = DrivingHabitDto(
    totalSessions = totalSessions,
    totalDrivingMinutes = totalDrivingMinutes,
    harshAccelCount = harshAccelCount,
    harshBrakeCount = harshBrakeCount,
    incautiousCount = incautiousCount,
    avgScore = avgScore,
    style = style.wire,
    eventsPerHour = round(eventsPerHour * 100) / 100,
)
