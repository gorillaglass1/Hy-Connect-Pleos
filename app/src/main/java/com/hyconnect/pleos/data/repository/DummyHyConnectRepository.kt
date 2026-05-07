package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.NetworkResult
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

    private suspend fun <T> dummyResult(block: () -> T): NetworkResult<T> =
        withContext(Dispatchers.IO) {
            delay(150)
            NetworkResult.Success(block())
        }
}
