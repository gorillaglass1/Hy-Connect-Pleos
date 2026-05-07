package com.hyconnect.pleos.data.model

data class VehicleState(
    val hydrogenPercent: Int = 78,
    val vehicleRangeKm: Int = 500,
    val message: String = "수소 충전이 충분합니다.",
)
