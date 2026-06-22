package com.hyconnect.pleos.vehicle.listener

import android.util.Log
import ai.pleos.playground.vehicle.listener.RangeRemainingListener
import com.hyconnect.pleos.vehicle.RangeRemainingTrigger

/**
 * RangeRemainingListener 구현체. 받은 주행가능거리를 km로 환산해 [RangeRemainingTrigger.updateRange]로 넘긴다.
 *
 * 단위: SDK는 AOSP RANGE_REMAINING 프로퍼티와 동일하게 **미터(m)** 단위로 통지한다.
 *       트리거/ViewModel/임계값은 km 기준이므로 여기서 m → km로 환산해 단위를 통일한다.
 */
class RangeRemainingListenerImpl(
    private val trigger: RangeRemainingTrigger,
) : RangeRemainingListener {

    override fun onRangeRemainingChanged(rangeRemaining: Float?) {
        if (rangeRemaining == null) {
            Log.w(LOG_TAG, "RangeRemaining null — ignored.")
            return
        }
        trigger.updateRange(rangeRemaining / METERS_PER_KM)
    }

    override fun onFailed(e: Exception) {
        Log.w(LOG_TAG, "RangeRemaining onFailed called.", e)
    }

    private companion object {
        const val LOG_TAG = "HyConnect.RangeListener"
        const val METERS_PER_KM = 1000f
    }
}
