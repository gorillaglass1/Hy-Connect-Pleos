package com.hyconnect.pleos.data.repository

import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.HyConnectService
import com.hyconnect.pleos.data.network.NetworkResult
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HyConnectRepositoryImplTest {
    @Test
    fun getVehicleStateReturnsSuccessWhenServiceResponds() = runBlocking {
        val expected = VehicleState(
            hydrogenPercent = 74,
            vehicleRangeKm = 455,
            message = "Enough hydrogen for current route",
        )
        val repository = HyConnectRepositoryImpl(
            service = FakeHyConnectService(vehicleState = expected),
        )

        val result = repository.getVehicleState()

        assertTrue(result is NetworkResult.Success)
        assertEquals(expected, (result as NetworkResult.Success).data)
    }

    @Test
    fun getRecommendedStationsReturnsSuccessWhenServiceResponds() = runBlocking {
        val stations = listOf(
            HydrogenStation(
                id = "station-1",
                name = "HyConnect Gangnam",
                address = "Seoul",
                status = "OPEN",
                pressureInfo = "700bar",
                distanceKm = 2.4,
                waitMinutes = 7,
                isRecommended = true,
            ),
        )
        val repository = HyConnectRepositoryImpl(
            service = FakeHyConnectService(stations = stations),
        )

        val result = repository.getRecommendedStations()

        assertTrue(result is NetworkResult.Success)
        assertEquals(stations, (result as NetworkResult.Success).data)
    }

    @Test
    fun serviceIOExceptionIsMappedToNetworkError() = runBlocking {
        val exception = IOException("timeout")
        val repository = HyConnectRepositoryImpl(
            service = FakeHyConnectService(vehicleStateException = exception),
        )

        val result = repository.getVehicleState()

        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertTrue(error.message.isNotBlank())
        assertSame(exception, error.cause)
    }

    private class FakeHyConnectService(
        private val vehicleState: VehicleState = VehicleState(),
        private val aiRecommendation: AiRecommendation = AiRecommendation(),
        private val stations: List<HydrogenStation> = emptyList(),
        private val vehicleStateException: Exception? = null,
    ) : HyConnectService {
        override suspend fun getVehicleState(): VehicleState {
            vehicleStateException?.let { throw it }
            return vehicleState
        }

        override suspend fun getAiRecommendation(): AiRecommendation = aiRecommendation

        override suspend fun getRecommendedStations(): List<HydrogenStation> = stations
    }
}
