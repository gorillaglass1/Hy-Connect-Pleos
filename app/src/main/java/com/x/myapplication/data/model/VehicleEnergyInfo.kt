package com.x.myapplication.data.model

data class VehicleEnergyInfo(
    val batteryPercent: Int? = null,
    val rangeKm: Double? = null,
    val efficiency: String? = null,
    val chargingState: String? = null,
    val status: String = "Pleos Vehicle 정보를 불러오는 중입니다.",
)
