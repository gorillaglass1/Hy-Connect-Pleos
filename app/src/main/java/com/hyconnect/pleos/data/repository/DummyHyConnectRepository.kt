package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.ChargingLogResponseDto
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.network.ReservationResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DummyHyConnectRepository : HyConnectRepository {
    override suspend fun getVehicleState(): NetworkResult<VehicleState> =
        dummyResult { DummyHyConnectData.vehicleState }

    override suspend fun getAiRecommendation(): NetworkResult<AiRecommendation> =
        dummyResult { DummyHyConnectData.aiRecommendation }

    override suspend fun getRecommendedStations(): NetworkResult<List<HydrogenStation>> =
        dummyResult { DummyHyConnectData.recommendedStations }

    override suspend fun createReservation(
        stationId: Int,
        chargerId: Int,
        userId: Int,
    ): NetworkResult<ReservationResponseDto> =
        dummyResult {
            ReservationResponseDto(
                hydrogenStationReservationId = 1,
                hydrogenChargerId = chargerId,
                hydrogenStationId = stationId,
                reservationStatus = "reserved",
                userId = userId,
                reservationTime = "2026-05-08T00:00:00Z",
                expireTime = "2026-05-08T00:10:00Z",
                createdAt = "2026-05-08T00:00:00Z",
            )
        }

    override suspend fun createChargingLog(
        userId: Int,
        stationId: Int,
        vehicleId: Int,
        startTime: String,
        endTime: String,
        chargedAmount: Double?,
        chargingCost: Double?,
        waitingTime: Int?,
    ): NetworkResult<ChargingLogResponseDto> =
        dummyResult {
            ChargingLogResponseDto(
                chargingLogId = 1,
                userId = userId,
                hydrogenStationId = stationId,
                vehicleId = vehicleId,
                startTime = startTime,
                endTime = endTime,
                chargedAmount = chargedAmount,
                chargingCost = chargingCost,
                waitingTime = waitingTime,
                createdAt = "2026-05-08T00:00:00Z",
            )
        }

    private suspend fun <T> dummyResult(block: () -> T): NetworkResult<T> =
        withContext(Dispatchers.IO) {
            delay(150)
            NetworkResult.Success(block())
        }
}
