package com.hyconnect.pleos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.repository.HyConnectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HyConnectUiState(
    val vehicleState: VehicleState = VehicleState(),
    val stations: List<HydrogenStation> = emptyList(),
    val aiRecommendation: AiRecommendation = AiRecommendation(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class HyConnectViewModel(
    private val repository: HyConnectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HyConnectUiState())
    val uiState: StateFlow<HyConnectUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            Log.d("HyConnect", "refresh started")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val vehicleStateRequest = async { repository.getVehicleState() }
            val recommendationRequest = async { repository.getAiRecommendation() }
            val stationsRequest = async { repository.getRecommendedStations() }

            val vehicleStateResult = vehicleStateRequest.await()
            val recommendationResult = recommendationRequest.await()
            val stationsResult = stationsRequest.await()

            val errorMessage = listOf(
                vehicleStateResult.errorOrNull(),
                recommendationResult.errorOrNull(),
                stationsResult.errorOrNull(),
            ).firstOrNull()
            Log.d(
                "HyConnect",
                "refresh completed vehicle=${vehicleStateResult is NetworkResult.Success} " +
                    "recommendation=${recommendationResult is NetworkResult.Success} " +
                    "stations=${stationsResult is NetworkResult.Success} error=$errorMessage",
            )

            _uiState.update { current ->
                current.copy(
                    vehicleState = vehicleStateResult.dataOrFallback(current.vehicleState),
                    aiRecommendation = recommendationResult.dataOrFallback(current.aiRecommendation),
                    stations = stationsResult.dataOrFallback(current.stations),
                    isLoading = false,
                    errorMessage = errorMessage,
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: HyConnectRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // TODO: Hilt/Koin 등 DI를 도입하면 ViewModel 의존성 주입을 프레임워크로 교체한다.
            if (modelClass.isAssignableFrom(HyConnectViewModel::class.java)) {
                return HyConnectViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun <T> NetworkResult<T>.dataOrFallback(fallback: T): T =
    when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Error -> fallback
    }

private fun NetworkResult<*>.errorOrNull(): String? =
    when (this) {
        is NetworkResult.Success -> null
        is NetworkResult.Error -> message
    }
