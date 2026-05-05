package com.x.myapplication.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.EvBattery
import ai.pleos.playground.vehicle.api.Odometer
import android.content.Context
import com.x.myapplication.data.model.VehicleEnergyInfo
import kotlin.coroutines.resume
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine

@Suppress("DEPRECATION")
class PleosVehicleClient(context: Context) {
    private val vehicle = Vehicle(context.applicationContext)

    fun initialize() {
        vehicle.initialize()
    }

    suspend fun loadEnergyInfo(): VehicleEnergyInfo = coroutineScope {
        val evBattery = vehicle.getEvBattery()
        val odometer = vehicle.getOdometer()

        val batteryLevelDeferred = async { evBattery.getBatteryLevelRaw() }
        val batteryCapacityDeferred = async { evBattery.getBatteryCapacityRaw() }
        val rangeDeferred = async { odometer.getRangeRemainingKm() }
        val chargeStateDeferred = async { evBattery.getChargeSwitchState() }

        val batteryLevel = batteryLevelDeferred.await()
        val batteryCapacity = batteryCapacityDeferred.await()
        val batteryPercent = calculateBatteryPercent(batteryLevel, batteryCapacity)
        val rangeKm = normalizeRangeKm(rangeDeferred.await())
        val chargeState = chargeStateDeferred.await()?.let { if (it) "충전 켜짐" else "충전 꺼짐" }

        val status = when {
            batteryPercent != null || rangeKm != null -> "Pleos Vehicle 연결됨"
            batteryLevel != null || batteryCapacity != null -> "배터리 원시값은 수신했지만 표시 가능한 퍼센트/주행거리로 환산하지 못했습니다."
            else -> "차량 에너지 API가 이 모델/에뮬레이터에서 제공되지 않습니다."
        }

        VehicleEnergyInfo(
            batteryPercent = batteryPercent,
            rangeKm = rangeKm,
            efficiency = formatBatteryCapacity(batteryCapacity),
            chargingState = chargeState,
            status = status,
        )
    }

    fun release() {
        vehicle.release()
    }

    private suspend fun EvBattery.getBatteryLevelRaw(): Float? = suspendCancellableCoroutine { continuation ->
        getEvBatteryLevel(
            { value -> if (continuation.isActive) continuation.resume(value) },
            { if (continuation.isActive) continuation.resume(null) },
        )
    }

    private suspend fun EvBattery.getBatteryCapacityRaw(): Float? = suspendCancellableCoroutine { continuation ->
        getEvBatteryCapacity(
            { value -> if (continuation.isActive) continuation.resume(value) },
            { if (continuation.isActive) continuation.resume(null) },
        )
    }

    private suspend fun EvBattery.getChargeSwitchState(): Boolean? = suspendCancellableCoroutine { continuation ->
        getEvChargeSwitchState(
            { value -> if (continuation.isActive) continuation.resume(value) },
            { if (continuation.isActive) continuation.resume(null) },
        )
    }

    private suspend fun Odometer.getRangeRemainingKm(): Double? = suspendCancellableCoroutine { continuation ->
        getRangeRemainingDistance(
            { value -> if (continuation.isActive) continuation.resume(value?.toDouble()) },
            { if (continuation.isActive) continuation.resume(null) },
        )
    }

    private fun calculateBatteryPercent(level: Float?, capacity: Float?): Int? {
        if (level == null) return null

        val percent = when {
            level in 0f..1f -> level * 100f
            level in 0f..100f -> level
            capacity != null && capacity > 0f -> (level / capacity) * 100f
            else -> null
        } ?: return null

        return percent
            .takeIf { it.isFinite() && it in 0f..100f }
            ?.toInt()
    }

    private fun formatBatteryCapacity(capacity: Float?): String? {
        return capacity
            ?.takeIf { it > 0f }
            ?.let { "%.1f kWh".format(it / 1_000f) }
    }

    private fun normalizeRangeKm(value: Double?): Double? {
        if (value == null || !value.isFinite() || value < 0.0) return null
        return when {
            value <= 2_000.0 -> value
            value <= 2_000_000.0 -> value / 1_000.0
            else -> null
        }
    }
}
