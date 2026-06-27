package com.hyconnect.pleos.vehicle.listener

import android.util.Log
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import com.hyconnect.pleos.vehicle.RangeRemainingTrigger
import com.hyconnect.pleos.vehicle.VehicleDrivingState

class GearSelectionListenerImpl(
    private val trigger: RangeRemainingTrigger,
    // 운전습관 세션 시작/종료 판정용. 주행상태가 바뀔 때마다 통지한다(없으면 무시).
    private val onDrivingState: ((VehicleDrivingState) -> Unit)? = null,
) : GearSelectionListener {

    override fun onSelected(gear: VehicleGear?) {
        val state = when (gear) {
            VehicleGear.GEAR_PARK,
            VehicleGear.GEAR_NEUTRAL -> VehicleDrivingState.STOPPED

            VehicleGear.GEAR_DRIVE,
            VehicleGear.GEAR_REVERSE,
            VehicleGear.GEAR_1, VehicleGear.GEAR_2, VehicleGear.GEAR_3,
            VehicleGear.GEAR_4, VehicleGear.GEAR_5, VehicleGear.GEAR_6,
            VehicleGear.GEAR_7, VehicleGear.GEAR_8, VehicleGear.GEAR_9 -> VehicleDrivingState.DRIVING

            VehicleGear.GEAR_UNKNOWN, null -> VehicleDrivingState.UNKNOWN
        }
        trigger.updateDrivingState(state)
        onDrivingState?.invoke(state)
    }

    override fun onFailed(e: Exception) {
        Log.w(LOG_TAG, "GearSelection onFailed called.", e)
    }

    private companion object {
        const val LOG_TAG = "HyConnect.GearListener"
    }
}
