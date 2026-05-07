package com.hyconnect.pleos.data.model

import com.google.gson.annotations.SerializedName

data class VehicleState(
    // TODO: 서버 /vehicle/state 응답 스키마가 확정되면 필드명과 타입을 실제 문서에 맞춘다.
    @SerializedName("hydrogen_percent")
    val hydrogenPercent: Int = 0,
    @SerializedName("vehicle_range_km")
    val vehicleRangeKm: Int = 0,
    @SerializedName("message")
    val message: String = "차량 상태를 불러오는 중입니다.",
)
