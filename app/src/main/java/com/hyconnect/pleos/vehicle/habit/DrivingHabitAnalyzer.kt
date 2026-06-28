package com.hyconnect.pleos.vehicle.habit

import kotlin.math.abs
import kotlin.math.round

/**
 * 샘플앱 `MainViewModel`의 운전습관 분석 규칙을 이식한 세션 단위 분석기.
 *
 * 주행 세션(기어 D 진입 ~ 정차/주차) 동안 속도/조향각 변화를 받아
 * 급가속·급정거·부주의 이벤트를 집계하고 종료 시 점수를 산출한다.
 * 영속 누적은 [DrivingHabitStore]가 맡고, 본 클래스는 현재 한 세션의 상태만 들고 있다.
 *
 * 임계값은 샘플앱과 동일(속도차 ±30km/h, 조향각 변화 50°)하게 두되 한곳에 상수로 모은다.
 * 호출은 단일 스레드(SDK 콜백/메인)에서 직렬로 들어온다고 가정한다.
 */
class DrivingHabitAnalyzer {
    private var active = false
    private var sessionStartMs = 0L
    private var harshAccel = 0
    private var harshBrake = 0
    private var incautious = 0

    // 직전 통지값. 첫 통지는 기준값만 잡고 변화량으로 세지 않는다(샘플앱과 동일).
    private var lastSpeedMps: Float? = null
    private var lastSteeringDeg: Float? = null

    val isActive: Boolean get() = active

    /**
     * 감점 사유(급가속/급정거/부주의)가 발생할 때마다 호출되는 콜백.
     * 세션 시작/종료 시에도 한 번씩 불려 UI가 현재 세션 상태를 실시간으로 반영하게 한다.
     * 보통 ViewModel이 이 콜백으로 화면(현재 주행 패널)을 즉시 갱신한다.
     */
    var onSessionUpdate: ((LiveDrivingSession) -> Unit)? = null

    fun startSession(nowMs: Long = System.currentTimeMillis()) {
        active = true
        sessionStartMs = nowMs
        harshAccel = 0
        harshBrake = 0
        incautious = 0
        lastSpeedMps = null
        lastSteeringDeg = null
        notifyUpdate()
    }

    /**
     * 속도 통지. 단위는 SDK/CarProperty와 동일한 m/s.
     * 직전 값과의 차이를 km/h로 환산해 ±[SPEED_DELTA_THRESHOLD_KMH] 이상이면 급가속/급정거로 센다.
     */
    fun onSpeed(metersPerSec: Float) {
        if (!active) return
        val prev = lastSpeedMps
        lastSpeedMps = metersPerSec
        if (prev == null) return
        val deltaKmh = round((metersPerSec - prev) * MPS_TO_KMH)
        when {
            deltaKmh >= SPEED_DELTA_THRESHOLD_KMH -> { harshAccel++; notifyUpdate() }
            deltaKmh <= -SPEED_DELTA_THRESHOLD_KMH -> { harshBrake++; notifyUpdate() }
        }
    }

    /** 조향각 통지(도). 직전 값과의 절대 변화가 [STEERING_DELTA_THRESHOLD_DEG] 이상이면 부주의로 센다. */
    fun onSteeringAngle(angleDeg: Float) {
        if (!active) return
        val prev = lastSteeringDeg
        lastSteeringDeg = angleDeg
        if (prev == null) return
        if (round(abs(angleDeg - prev)) >= STEERING_DELTA_THRESHOLD_DEG) {
            incautious++
            notifyUpdate()
        }
    }

    /** 현재 진행 중인 세션의 실시간 스냅샷. 감점이 발생할 때마다 [onSessionUpdate]로 전달된다. */
    fun liveSnapshot(): LiveDrivingSession {
        val events = harshAccel + harshBrake + incautious
        val runningScore = if (active) {
            (BASE_SCORE - events * PENALTY_PER_EVENT).coerceIn(0, BASE_SCORE)
        } else {
            BASE_SCORE
        }
        return LiveDrivingSession(
            active = active,
            harshAccelCount = harshAccel,
            harshBrakeCount = harshBrake,
            incautiousCount = incautious,
            runningScore = runningScore,
        )
    }

    private fun notifyUpdate() {
        onSessionUpdate?.invoke(liveSnapshot())
    }

    /** 세션 종료. 활성 세션이 없으면 null을 반환한다. */
    fun finishSession(nowMs: Long = System.currentTimeMillis()): DrivingSession? {
        if (!active) return null
        active = false
        notifyUpdate()
        val minutes = ((nowMs - sessionStartMs) / MILLIS_PER_MINUTE).toInt().coerceAtLeast(0)
        val events = harshAccel + harshBrake + incautious
        val score = (BASE_SCORE - events * PENALTY_PER_EVENT).coerceIn(0, BASE_SCORE)
        return DrivingSession(
            harshAccelCount = harshAccel,
            harshBrakeCount = harshBrake,
            incautiousCount = incautious,
            score = score,
            durationMinutes = minutes,
            points = pointsFor(minutes, score),
        )
    }

    companion object {
        // 샘플앱 이식 임계값.
        const val SPEED_DELTA_THRESHOLD_KMH = 30f
        const val STEERING_DELTA_THRESHOLD_DEG = 50f
        const val BASE_SCORE = 100
        const val PENALTY_PER_EVENT = 5

        // 누적 주행 포인트(리워드) 산정 계수.
        //   세션 완료 보너스 + 주행 1분당 포인트 + (안전운전 점수 비례) 보너스.
        // 짧게라도 안전하게 완주하면 포인트가 쌓이고, 거칠게 운전하면 안전 보너스가 깎인다.
        const val POINTS_PER_SESSION = 20      // 세션 완주 기본 보너스
        const val POINTS_PER_MINUTE = 5        // 주행 시간 보상(1분당)
        const val SAFETY_BONUS_MAX = 50        // 만점(100) 주행 시 추가되는 안전운전 보너스 상한

        /** 세션 한 건이 적립하는 누적 포인트. 주행 시간 보상 + 안전운전 점수 비례 보너스. */
        fun pointsFor(durationMinutes: Int, score: Int): Int {
            val timeReward = durationMinutes.coerceAtLeast(0) * POINTS_PER_MINUTE
            val safetyBonus = round(SAFETY_BONUS_MAX * score / 100f).toInt()
            return POINTS_PER_SESSION + timeReward + safetyBonus
        }

        private const val MPS_TO_KMH = 3.6f
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}

/** 한 주행 세션의 집계 결과. */
data class DrivingSession(
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val incautiousCount: Int,
    val score: Int,
    val durationMinutes: Int,
    // 이 세션이 적립한 누적 주행 포인트(리워드). [DrivingHabitAnalyzer.pointsFor]로 산정.
    val points: Int,
)

/**
 * 진행 중인 주행 세션의 실시간 상태. 감점 사유가 발생할 때마다 갱신돼 화면에 즉시 반영된다.
 * (영속 누적 전의 "현재 주행" 값으로, 세션이 끝나면 [DrivingHabitProfile]에 합산된다.)
 */
data class LiveDrivingSession(
    val active: Boolean = false,
    val harshAccelCount: Int = 0,
    val harshBrakeCount: Int = 0,
    val incautiousCount: Int = 0,
    // 현재까지의 감점을 반영한 진행 점수(0~100).
    val runningScore: Int = DrivingHabitAnalyzer.BASE_SCORE,
)
