package com.hyconnect.pleos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.repository.HyConnectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 연료 상태에 따른 화면 모드. 주행가능거리 임계값을 기준으로 전환한다. */
enum class FuelMode {
    /** 연료 충분 → 날씨·교통 대시보드(추후 제작) */
    SUFFICIENT,

    /** 연료 부족 → 자연어 충전소 추천 */
    LOW,
}

data class HyConnectUiState(
    val vehicleState: VehicleState = VehicleState(),
    val fuelMode: FuelMode = FuelMode.SUFFICIENT,
    val stations: List<HydrogenStation> = emptyList(),
    val nlQuery: String = DEFAULT_NL_QUERY,
    val driverMessage: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_NL_QUERY = "제일 가까운 충전소 추천해줘"
    }
}

class HyConnectViewModel(
    private val repository: HyConnectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HyConnectUiState())
    val uiState: StateFlow<HyConnectUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** 차량 상태를 불러오고, 주행가능거리가 임계값 이하이면 자연어 추천을 호출한다. */
    fun refresh() {
        viewModelScope.launch {
            Log.d("HyConnect", "refresh started")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val vehicleResult = repository.getVehicleState()
            val vehicleState = vehicleResult.dataOrFallback(_uiState.value.vehicleState)
            val mode = fuelModeFor(vehicleState.vehicleRangeKm)

            if (mode == FuelMode.LOW) {
                val recResult = repository.getNlRecommendedStations(
                    nlQuery = _uiState.value.nlQuery,
                    remainingRange = vehicleState.vehicleRangeKm,
                )
                val recommendation = recResult.dataOrFallback(EMPTY_RECOMMENDATION)
                Log.d("HyConnect", "LOW fuel mode, stations=${recommendation.stations.size}")
                _uiState.update {
                    it.copy(
                        vehicleState = vehicleState,
                        fuelMode = FuelMode.LOW,
                        stations = recommendation.stations,
                        driverMessage = recommendation.driverMessage,
                        isLoading = false,
                        errorMessage = vehicleResult.errorOrNull() ?: recResult.errorOrNull(),
                    )
                }
            } else {
                Log.d("HyConnect", "SUFFICIENT fuel mode")
                _uiState.update {
                    it.copy(
                        vehicleState = vehicleState,
                        fuelMode = FuelMode.SUFFICIENT,
                        stations = emptyList(),
                        driverMessage = null,
                        isLoading = false,
                        errorMessage = vehicleResult.errorOrNull(),
                    )
                }
            }
        }
    }

    /** 사용자가 입력/음성으로 자연어 질의를 바꾸면 추천을 다시 호출한다. */
    fun searchStations(query: String) {
        val trimmed = query.trim().ifBlank { _uiState.value.nlQuery }
        _uiState.update { it.copy(nlQuery = trimmed, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val range = _uiState.value.vehicleState.vehicleRangeKm
            val recResult = repository.getNlRecommendedStations(nlQuery = trimmed, remainingRange = range)
            val recommendation = recResult.dataOrFallback(
                StationRecommendation(driverMessage = null, stations = _uiState.value.stations),
            )
            _uiState.update {
                it.copy(
                    fuelMode = FuelMode.LOW,
                    stations = recommendation.stations,
                    driverMessage = recommendation.driverMessage,
                    isLoading = false,
                    errorMessage = recResult.errorOrNull(),
                )
            }
        }
    }

    /** 검색 실행 없이 입력창 텍스트만 갱신한다. */
    fun updateNlQuery(query: String) {
        _uiState.update { it.copy(nlQuery = query) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fuelModeFor(remainingRangeKm: Int): FuelMode =
        if (remainingRangeKm <= LOW_RANGE_THRESHOLD_KM) FuelMode.LOW else FuelMode.SUFFICIENT

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

    companion object {
        /** 주행가능거리가 이 값(km) 이하면 충전소 추천 모드로 전환한다. */
        const val LOW_RANGE_THRESHOLD_KM = 100

        private val EMPTY_RECOMMENDATION = StationRecommendation(driverMessage = null, stations = emptyList())
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
