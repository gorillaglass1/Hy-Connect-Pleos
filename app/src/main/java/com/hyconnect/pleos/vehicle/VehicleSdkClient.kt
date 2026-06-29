package com.hyconnect.pleos.vehicle

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import ai.pleos.playground.vehicle.Vehicle
import com.hyconnect.pleos.vehicle.habit.DrivingHabitSignalListener
import com.hyconnect.pleos.vehicle.listener.GearSelectionListenerImpl
import com.hyconnect.pleos.vehicle.listener.RangeRemainingListenerImpl
import com.hyconnect.pleos.vehicle.listener.SteeringWheelListenerImpl

/**
 * Pleos Vehicle SDK 라이프사이클 + 리스너 등록/해제 래퍼.
 *
 * 사용 패턴:
 *   Activity.onCreate  → [initialize] + [registerListeners]
 *   Activity.onDestroy → [unregisterListeners] + [release]
 *
 * 주행가능거리는 Odometer의 RangeRemaining, 주행/정차 판별은 Gear 상태로 받는다.
 * 운전습관 분석을 위해 [registerListeners]에 [DrivingHabitSignalListener]를 넘기면
 * 추가로 조향각(Pleos SDK SteeringWheel)·속도(Google android.car CarProperty)를 구독해 전달한다.
 *
 * Pleos 차량 서비스가 없는 일반 에뮬레이터/기기에서는 SDK/Car 호출이 실패할 수 있으므로
 * 모든 접근을 예외로 감싸 앱이 죽지 않고 폴백(서버 임시 상태)으로 동작하게 한다.
 */
class VehicleSdkClient(context: Context) {

    private val appContext: Context = context.applicationContext
    private val vehicle: Vehicle = Vehicle(appContext)

    private var rangeListener: RangeRemainingListenerImpl? = null
    private var gearListener: GearSelectionListenerImpl? = null

    // 운전습관용(선택). habitListener가 없으면 등록하지 않는다.
    private var steeringListener: SteeringWheelListenerImpl? = null
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private var speedCallback: CarPropertyManager.CarPropertyEventCallback? = null

    // CAR_SPEED 런타임 권한이 늦게 허용됐을 때 속도 구독을 다시 시도하려고 보관한다.
    private var pendingHabitListener: DrivingHabitSignalListener? = null

    fun initialize() {
        runCatching {
            Log.d(LOG_TAG, "Vehicle SDK initialize")
            vehicle.initialize()
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK initialize 실패 — 폴백 동작.", it) }
    }

    fun registerListeners(
        trigger: RangeRemainingTrigger,
        habitListener: DrivingHabitSignalListener? = null,
    ) {
        val range = RangeRemainingListenerImpl(trigger)
        val gear = GearSelectionListenerImpl(
            trigger = trigger,
            onDrivingState = habitListener?.let { listener ->
                { state -> listener.onDrivingStateChanged(state == VehicleDrivingState.DRIVING) }
            },
        )
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

        if (habitListener != null) {
            pendingHabitListener = habitListener
            registerSteering(habitListener)
            // 속도는 CAR_SPEED(런타임/dangerous) 권한이 있어야 구독된다. 없으면 조용히 건너뛰고,
            // Activity가 권한을 받은 뒤 [onSpeedPermissionGranted]로 다시 시도한다.
            registerSpeed(habitListener)
        }
    }

    /**
     * CAR_SPEED 런타임 권한이 허용된 직후 Activity가 호출한다.
     * onCreate 시점엔 권한이 없어 건너뛴 속도 구독을 여기서 (재)시도한다.
     */
    fun onSpeedPermissionGranted() {
        pendingHabitListener?.let { registerSpeed(it) }
    }

    /** 조향각(Pleos SDK SteeringWheel) 구독. 실패해도 운전습관 분석만 일부 제한되고 앱은 계속 동작한다. */
    private fun registerSteering(habitListener: DrivingHabitSignalListener) {
        runCatching {
            val listener = SteeringWheelListenerImpl { angle -> habitListener.onSteeringAngle(angle) }
            steeringListener = listener
            vehicle.getSteeringWheel().registerSteeringWheelAngle(listener)
        }.onFailure { Log.w(LOG_TAG, "조향각 구독 실패 — 부주의 감지 제한.", it) }
    }

    /**
     * 속도(Google android.car CarProperty) 구독. PERF_VEHICLE_SPEED_DISPLAY는 m/s 단위다.
     * CAR_SPEED는 dangerous 권한이라 미허용이면 registerCallback이 SecurityException을 던진다.
     * 권한이 없으면 구독을 건너뛰어(급가속/급정거만 비활성) 권한 팝업 이후 재시도에 맡긴다.
     */
    private fun registerSpeed(habitListener: DrivingHabitSignalListener) {
        if (speedCallback != null) return // 이미 구독 중이면 중복 등록하지 않는다.
        if (!hasSpeedPermission()) {
            Log.d(LOG_TAG, "CAR_SPEED 권한 없음 — 속도 구독 보류(권한 허용 후 재시도).")
            return
        }
        runCatching {
            val createdCar = Car.createCar(appContext)
            car = createdCar
            val manager = createdCar.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            carPropertyManager = manager
            val callback = object : CarPropertyManager.CarPropertyEventCallback {
                override fun onChangeEvent(value: CarPropertyValue<*>?) {
                    val v = value ?: return
                    if (v.propertyId == VehiclePropertyIds.PERF_VEHICLE_SPEED_DISPLAY) {
                        (v.value as? Float)?.let(habitListener::onSpeed)
                    }
                }

                override fun onErrorEvent(propertyId: Int, areaId: Int) {
                    Log.w(LOG_TAG, "speed property error: id=$propertyId area=$areaId")
                }
            }
            speedCallback = callback
            val ok = manager.registerCallback(
                callback,
                VehiclePropertyIds.PERF_VEHICLE_SPEED_DISPLAY,
                CarPropertyManager.SENSOR_RATE_ONCHANGE,
            )
            Log.d(LOG_TAG, "속도 구독 등록 결과=$ok (PERF_VEHICLE_SPEED_DISPLAY)")
        }.onFailure { Log.w(LOG_TAG, "속도 구독 실패 — 급가속/급정거 감지 제한.", it) }
    }

    fun unregisterListeners() {
        runCatching {
            rangeListener?.let { vehicle.getOdometer().unregisterRangeRemainingListener(it) }
            gearListener?.let { vehicle.getGear().unregisterGearSelection(it) }
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK 리스너 해제 실패.", it) }
        runCatching {
            steeringListener?.let { vehicle.getSteeringWheel().unregisterSteeringWheelAngle(it) }
        }.onFailure { Log.w(LOG_TAG, "조향각 리스너 해제 실패.", it) }
        runCatching {
            speedCallback?.let { cb ->
                carPropertyManager?.unregisterCallback(cb, VehiclePropertyIds.PERF_VEHICLE_SPEED_DISPLAY)
            }
            car?.disconnect()
        }.onFailure { Log.w(LOG_TAG, "속도 구독 해제 실패.", it) }
        rangeListener = null
        gearListener = null
        steeringListener = null
        speedCallback = null
        carPropertyManager = null
        car = null
        pendingHabitListener = null
    }

    private fun hasSpeedPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, SPEED_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED

    fun release() {
        runCatching {
            Log.d(LOG_TAG, "Vehicle SDK release")
            vehicle.release()
        }.onFailure { Log.w(LOG_TAG, "Vehicle SDK release 실패.", it) }
    }

    companion object {
        /** 속도(PERF_VEHICLE_SPEED_DISPLAY) 구독에 필요한 dangerous 런타임 권한. */
        const val SPEED_PERMISSION: String = Car.PERMISSION_SPEED

        private const val LOG_TAG = "HyConnect.VehicleSdk"
    }
}
