package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class VehicleState(
    // 서버 차량 등록 정보와 SDK/실시간 상태를 합친 Compose UI 표시용 모델이다.
    @SerializedName("vehicle_range_km")
    val vehicleRangeKm: Int = 0,
    @SerializedName("message")
    val message: String = "차량 상태를 불러오는 중입니다.",
    // 서버(또는 대시보드)가 연료 잔량(%)을 직접 내려준 경우의 값. null이면 주행거리에서 환산한다.
    @SerializedName("fuel_percent")
    val fuelPercent: Int? = null,
) {
    /**
     * 게이지(%) 표시용.
     * 서버가 [fuelPercent]를 직접 주면 그 값을 그대로 쓰고(권위 있는 단일 출처),
     * 없을 때만 주행가능거리를 완충 기준거리([FULL_RANGE_KM]) 대비 비율로 환산한다.
     */
    val hydrogenPercent: Int
        get() = (fuelPercent ?: ((vehicleRangeKm.toFloat() / FULL_RANGE_KM) * 100f).roundToInt())
            .coerceIn(0, 100)

    companion object {
        /**
         * 게이지(%) 환산용 완충 기준 주행가능거리(km) = 차량 최대 연료량 사이즈.
         * 차량(예: NEXO ~600km)에 맞춰 이 값 하나만 조정하면 된다.
         */
        const val FULL_RANGE_KM = 500
    }
}
