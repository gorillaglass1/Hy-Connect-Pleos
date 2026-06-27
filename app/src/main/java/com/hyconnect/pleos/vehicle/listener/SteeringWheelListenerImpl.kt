package com.hyconnect.pleos.vehicle.listener

import android.util.Log
import ai.pleos.playground.vehicle.listener.SteeringWheelAngleListener

/**
 * Pleos Vehicle SDK의 조향각 통지를 받아 콜백으로 전달한다.
 * 운전습관 분석(부주의 감지)의 입력 신호로 쓴다. null 각도는 무시한다.
 */
class SteeringWheelListenerImpl(
    private val onAngle: (Float) -> Unit,
) : SteeringWheelAngleListener {

    override fun onSteeringWheelAngleUpdated(angle: Float?) {
        if (angle != null) onAngle(angle)
    }

    override fun onFailed(e: Exception) {
        Log.w(LOG_TAG, "SteeringWheel angle onFailed called.", e)
    }

    private companion object {
        const val LOG_TAG = "HyConnect.SteeringListener"
    }
}
