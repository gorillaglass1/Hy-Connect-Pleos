package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class VehicleState(
    // 서버 차량 등록 정보와 SDK/실시간 상태를 합친 Compose UI 표시용 모델이다.
    @SerializedName("hydrogen_percent")
    val hydrogenPercent: Int = 0,
    @SerializedName("vehicle_range_km")
    val vehicleRangeKm: Int = 0,
    @SerializedName("message")
    val message: String = "차량 상태를 불러오는 중입니다.",
)
