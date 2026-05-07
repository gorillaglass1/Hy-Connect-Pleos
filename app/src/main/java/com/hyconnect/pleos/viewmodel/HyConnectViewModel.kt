package com.hyconnect.pleos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.repository.HyConnectRepository
import com.hyconnect.pleos.data.repository.MockHyConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HyConnectUiState(
    val hydrogenPercent: Int = 78,
    val vehicleRangeKm: Int = 500,
    val vehicleState: VehicleState = VehicleState(),
    val stations: List<HydrogenStation> = emptyList(),
    val aiRecommendation: AiRecommendation = AiRecommendation(),
    val isLoading: Boolean = true,
)

class HyConnectViewModel(
    private val repository: HyConnectRepository = MockHyConnectRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HyConnectUiState())
    val uiState: StateFlow<HyConnectUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    fun loadMockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val vehicleState = repository.getVehicleState()
            val aiRecommendation = repository.getAiRecommendation()
            val stations = repository.getRecommendedStations()

            _uiState.update {
                it.copy(
                    hydrogenPercent = vehicleState.hydrogenPercent,
                    vehicleRangeKm = vehicleState.vehicleRangeKm,
                    vehicleState = vehicleState,
                    aiRecommendation = aiRecommendation,
                    stations = stations,
                    isLoading = false,
                )
            }
        }
    }
}
