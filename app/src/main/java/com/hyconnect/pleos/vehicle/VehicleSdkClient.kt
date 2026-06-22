package com.hyconnect.pleos.vehicle

import android.content.Context
import android.util.Log
import ai.pleos.playground.vehicle.Vehicle
import com.hyconnect.pleos.vehicle.listener.GearSelectionListenerImpl
import com.hyconnect.pleos.vehicle.listener.RangeRemainingListenerImpl

/**
 * Pleos Vehicle SDK 라이프사이클 + 리스너 등록/해제 래퍼.
 *
 * 사용 패턴:
 *   Activity.onCreate  → [initialize] + [registerListeners]
 *   Activity.onDestroy → [unregisterListeners] + [release]
 *
 * 주행가능거리는 Odometer의 RangeRemaining, 주행/정차 판별은 Gear 상태로 받는다.
 * Pleos 차량 서비스가 없는 일반 에뮬레이터/기기에서는 SDK 호출이 실패할 수 있으므로
 * 모든 SDK 접근을 예외로 감싸 앱이 죽지 않고 폴백(서버 임시 상태)으로 동작하게 한다.
 */
class VehicleSdkClient(context: Context) {

    private val vehicle: Vehicle = Vehicle(context.applicationContext)

    private var rangeListener: RangeRemainingListenerImpl? = null
    private var gearListener: GearSelectionListenerImpl? = null

    fun initialize() {
        runCatching {
            Log.d(LOG_TAG, "Vehicle SDK initialize")
            vehicle.initialize()
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK initialize 실패 — 폴백 동작.", it) }
    }

    fun registerListeners(trigger: RangeRemainingTrigger) {
        val range = RangeRemainingListenerImpl(trigger)
        val gear = GearSelectionListenerImpl(trigger)
        rangeListener = range
        gearListener = gear
        runCatching {
            vehicle.getOdometer().registerRangeRemainingListener(range)
            vehicle.getGear().registerGearSelection(gear)
            // 등록 직후 현재 주행가능거리를 한 번 끌어와 첫 화면에 즉시 반영한다.
            // 단위 환산(m → km)은 리스너가 전담하므로 리스너를 그대로 경유시킨다.
            vehicle.getOdometer().getRangeRemainingDistance(
                { meters -> range.onRangeRemainingChanged(meters) },
                { e -> Log.w(LOG_TAG, "초기 주행가능거리 조회 실패.", e) },
            )
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK 리스너 등록 실패 — 폴백 동작.", it) }
    }

    fun unregisterListeners() {
        runCatching {
            rangeListener?.let { vehicle.getOdometer().unregisterRangeRemainingListener(it) }
            gearListener?.let { vehicle.getGear().unregisterGearSelection(it) }
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK 리스너 해제 실패.", it) }
        rangeListener = null
        gearListener = null
    }

    fun release() {
        runCatching {
            Log.d(LOG_TAG, "Vehicle SDK release")
            vehicle.release()
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK release 실패.", it) }
    }

    private companion object {
        const val LOG_TAG = "HyConnect.VehicleSdk"
    }
}
