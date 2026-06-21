package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.ChargingLogResponseDto
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.network.UserPreferenceResponseDto
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

    override suspend fun getNlRecommendedStations(
        nlQuery: String,
        remainingRange: Int,
        userId: Int?,
    ): NetworkResult<StationRecommendation> =
        dummyResult {
            StationRecommendation(
                driverMessage = "주행가능거리 ${remainingRange}km 남았어요. \"$nlQuery\" 기준으로 추천했어요.",
                stations = DummyHyConnectData.recommendedStations,
            )
        }

    override suspend fun submitStationSelection(
        chrstnMno: String,
        userId: Int,
    ): NetworkResult<UserPreferenceResponseDto> =
        dummyResult {
            UserPreferenceResponseDto(
                userId = userId,
                weightPrice = "1.00",
                weightWaitingTime = "1.00",
                weightDistance = "1.00",
                weightFacilities = "1.00",
                safetyMargin = "1.10",
                createdAt = "2026-05-08T00:00:00Z",
                updatedAt = "2026-05-08T00:00:00Z",
            )
        }

    override suspend fun createChargingLog(
        chrstnMno: String,
        startTime: String,
        endTime: String,
        userId: Int,
        chargedAmount: Double?,
        chargingCost: Double?,
        waitingTime: Int?,
    ): NetworkResult<ChargingLogResponseDto> =
        dummyResult {
            ChargingLogResponseDto(
                chargingLogId = 1,
                userId = userId,
                chrstnMno = chrstnMno,
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
