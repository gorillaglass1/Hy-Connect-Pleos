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
import kotlin.math.roundToInt

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

    // Vehicle SDK가 통지한 최신 주행가능거리(km). null이면 아직 통지 전(또는 SDK 비연동 환경).
    // 통지 이후에는 이 값이 차량 상태의 단일 출처가 되어 서버 임시값보다 우선한다.
    @Volatile
    private var sdkRangeKm: Int? = null

    init {
        refresh()
    }

    /**
     * Vehicle SDK(Odometer RangeRemaining)가 실시간 주행가능거리를 통지할 때 호출한다.
     * 정책: 주행 여부와 무관하게 임계값 이하이면 즉시 충전소 추천 모드(LOW)로 전환한다.
     */
    fun onVehicleRangeUpdated(rangeKm: Int) {
        sdkRangeKm = rangeKm
        val previousMode = _uiState.value.fuelMode
        val mode = fuelModeFor(rangeKm)
        Log.d("HyConnect", "SDK range=${rangeKm}km → $mode")

        _uiState.update {
            it.copy(
                vehicleState = it.vehicleState.copy(
                    vehicleRangeKm = rangeKm,
                    hydrogenPercent = fuelPercentFor(rangeKm),
                ),
                fuelMode = mode,
            )
        }

        when (mode) {
            // LOW로 처음 진입했거나 추천 목록이 비어 있을 때만 재요청한다(매 통지마다 호출 방지).
            FuelMode.LOW ->
                if (previousMode != FuelMode.LOW || _uiState.value.stations.isEmpty()) {
                    loadRecommendations()
                }

            FuelMode.SUFFICIENT ->
                _uiState.update { it.copy(stations = emptyList(), driverMessage = null) }
        }
    }

    /** 차량 상태를 불러오고, 주행가능거리가 임계값 이하이면 자연어 추천을 호출한다. */
    fun refresh() {
        viewModelScope.launch {
            Log.d("HyConnect", "refresh started")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val vehicleResult = repository.getVehicleState()
            val baseState = vehicleResult.dataOrFallback(_uiState.value.vehicleState)
            // SDK가 이미 실거리를 통지했다면 그 값을 우선한다. 아니면 서버 임시값(연동 전 폴백).
            val rangeKm = sdkRangeKm ?: baseState.vehicleRangeKm
            // 게이지(%)는 주행가능거리에서 환산한다. SDK엔 수소 탱크/연료 레벨 API가 없고,
            // RANGE_REMAINING은 EV·수소·내연 모두에서 동작하므로 차종 무관하게 일관된 게이지를 준다.
            val vehicleState = baseState.copy(
                vehicleRangeKm = rangeKm,
                hydrogenPercent = fuelPercentFor(rangeKm),
            )
            val mode = fuelModeFor(rangeKm)

            _uiState.update {
                it.copy(
                    vehicleState = vehicleState,
                    fuelMode = mode,
                    errorMessage = vehicleResult.errorOrNull(),
                )
            }

            if (mode == FuelMode.LOW) {
                loadRecommendations()
            } else {
                Log.d("HyConnect", "SUFFICIENT fuel mode")
                _uiState.update {
                    it.copy(stations = emptyList(), driverMessage = null, isLoading = false)
                }
            }
        }
    }

    /** 현재 주행가능거리를 기준으로 자연어 충전소 추천을 받아 LOW 화면을 채운다. */
    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val range = _uiState.value.vehicleState.vehicleRangeKm
            val recResult = repository.getNlRecommendedStations(
                nlQuery = _uiState.value.nlQuery,
                remainingRange = range,
            )
            val recommendation = recResult.dataOrFallback(EMPTY_RECOMMENDATION)
            Log.d("HyConnect", "LOW fuel mode, stations=${recommendation.stations.size}")
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

    /**
     * 추천 카드에서 충전소를 경로로 선택(경로안내)할 때 호출한다.
     * 선택한 충전소 관리번호(chrstn_mno = [HydrogenStation.id])로 선호 가중치 학습을 서버에 전송한다.
     */
    fun selectStationForRoute(station: HydrogenStation) {
        viewModelScope.launch {
            val result = repository.submitStationSelection(station.id)
            if (result is NetworkResult.Error) {
                Log.w("HyConnect", "preference learning failed: ${result.message}")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fuelModeFor(remainingRangeKm: Int): FuelMode =
        if (remainingRangeKm <= LOW_RANGE_THRESHOLD_KM) FuelMode.LOW else FuelMode.SUFFICIENT

    /** 주행가능거리를 완충 기준거리 대비 비율(%)로 환산한다. 게이지 표시용. */
    private fun fuelPercentFor(remainingRangeKm: Int): Int =
        ((remainingRangeKm.toFloat() / FULL_RANGE_KM) * 100f).roundToInt().coerceIn(0, 100)

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

        /**
         * 게이지(%) 환산용 완충 기준 주행가능거리(km).
         * 수소 탱크 레벨 API가 없어 주행가능거리를 이 값 대비 비율로 표시한다.
         * 차량(예: NEXO ~600km)에 맞춰 조정한다.
         */
        const val FULL_RANGE_KM = 600

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
