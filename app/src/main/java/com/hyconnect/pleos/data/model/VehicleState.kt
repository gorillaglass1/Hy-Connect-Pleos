package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class VehicleState(
    // 서버 차량 등록 정보와 SDK/실시간 상태를 합친 Compose UI 표시용 모델이다.
    @SerializedName("vehicle_range_km")
    val vehicleRangeKm: Int = 0,
    @SerializedName("message")
    val message: String = "차량 상태를 불러오는 중입니다.",
) {
    /**
     * 게이지(%) 표시용. 주행가능거리를 완충 기준거리([FULL_RANGE_KM]) 대비 비율로 환산한다.
     * 수소 탱크 레벨 API가 없어 별도 입력값 없이 주행가능거리에서 파생한다.
     * %의 단일 출처이므로 [FULL_RANGE_KM]만 바꾸면 모든 화면의 게이지가 함께 갱신된다.
     */
    val hydrogenPercent: Int
        get() = ((vehicleRangeKm.toFloat() / FULL_RANGE_KM) * 100f).roundToInt().coerceIn(0, 100)

    companion object {
        /**
         * 게이지(%) 환산용 완충 기준 주행가능거리(km) = 차량 최대 연료량 사이즈.
         * 차량(예: NEXO ~600km)에 맞춰 이 값 하나만 조정하면 된다.
         */
        const val FULL_RANGE_KM = 500
    }
}
