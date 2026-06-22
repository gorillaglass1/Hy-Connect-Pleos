package com.hyconnect.pleos.vehicle

import android.util.Log

/**
 * 주행가능거리(km)와 주행상태(DRIVING/STOPPED)를 모아 정책에 따라 콜백을 분기한다.
 *
 * 정책(docs/pleos-vehicle-sdk.md §0-2, §6):
 * - 주행 중 + 임계값 이하: 화면 자동 전환·팝업 금지. [onDrivingAndLow] 호출(현재는 로그만).
 * - 정차/주차 + 임계값 이하: [onStoppedAndLow] 호출. 화면 전환은 ViewModel.fuelMode 전환으로 처리.
 * - 어느 경우든 SDK가 새 값을 통지할 때마다 [onRangeUpdated]를 호출해
 *   ViewModel의 range 값을 동기화한다. 이때 isDriving 플래그를 함께 넘겨서
 *   ViewModel 측에서도 주행 중 가드(자동 mode 전환 금지)를 걸 수 있게 한다.
 *
 * @param lowRangeThresholdKm LOW 판정 임계값. 기존 HyConnectViewModel.LOW_RANGE_THRESHOLD_KM 주입.
 * @param onRangeUpdated 주행 여부와 무관하게 항상 호출. (km, isDriving) 시그니처.
 * @param onDrivingAndLow 주행 중 + LOW 정책 슬롯. 자동 화면 전환 금지에 따라 로그/음성 안내 슬롯.
 * @param onStoppedAndLow 정차/주차 + LOW 정책 슬롯. 충전소 추천 화면 전환 트리거.
 */
class RangeRemainingTrigger(
    private val lowRangeThresholdKm: Int,
    private val onRangeUpdated: (rangeKm: Float, isDriving: Boolean) -> Unit,
    private val onDrivingAndLow: (rangeKm: Float) -> Unit,
    private val onStoppedAndLow: (rangeKm: Float) -> Unit,
) {
    @Volatile private var latestRangeKm: Float? = null
    @Volatile private var drivingState: VehicleDrivingState = VehicleDrivingState.UNKNOWN

    fun updateRange(rangeKm: Float) {
        latestRangeKm = rangeKm
        evaluate()
    }

    fun updateDrivingState(state: VehicleDrivingState) {
        drivingState = state
        evaluate()
    }

    private fun evaluate() {
        val km = latestRangeKm ?: return
        val isDriving = drivingState == VehicleDrivingState.DRIVING

        onRangeUpdated(km, isDriving)

        if (km > lowRangeThresholdKm) return

        when (drivingState) {
            VehicleDrivingState.DRIVING -> onDrivingAndLow(km)
            VehicleDrivingState.STOPPED -> onStoppedAndLow(km)
            VehicleDrivingState.UNKNOWN -> {
                // 주행 여부 미상 → 안전 측 동작: 어느 콜백도 부르지 않는다.
                // 다음 통지에서 drivingState가 확정되면 evaluate()가 다시 호출돼 재평가된다.
                Log.d(LOG_TAG, "range LOW but driving state UNKNOWN: ${km}km — no action.")
            }
        }
    }

    private companion object {
        const val LOG_TAG = "HyConnect.RangeTrigger"
    }
}
