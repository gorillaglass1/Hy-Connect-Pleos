package com.hyconnect.pleos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.StationRecommendation
import com.hyconnect.pleos.data.model.SufficientDashboard
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.network.NetworkResult
import com.hyconnect.pleos.data.repository.HyConnectRepository
import com.hyconnect.pleos.vehicle.habit.DrivingHabitAnalyzer
import com.hyconnect.pleos.vehicle.habit.DrivingHabitProfile
import com.hyconnect.pleos.vehicle.habit.DrivingHabitSignalListener
import com.hyconnect.pleos.vehicle.habit.DrivingHabitStore
import com.hyconnect.pleos.voice.withJosa
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    // 연료 충분 화면(SUFFICIENT)의 서버 드리븐 대시보드. 아직 로드 전이면 null.
    val dashboard: SufficientDashboard? = null,
    // 로컬 누적 운전습관 프로파일. 상단 운전 점수 패널에 표시하고 서버 개인화 요청에도 쓴다.
    val drivingHabit: DrivingHabitProfile = DrivingHabitProfile(),
    val nlQuery: String = DEFAULT_NL_QUERY,
    val driverMessage: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    companion object {
        const val DEFAULT_NL_QUERY = ""
    }
}

class HyConnectViewModel(
    private val repository: HyConnectRepository,
    private val habitStore: DrivingHabitStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HyConnectUiState())
    val uiState: StateFlow<HyConnectUiState> = _uiState.asStateFlow()

    // 한 주행 세션의 운전습관 집계기(메모리). 세션 종료 시 결과를 habitStore에 누적한다.
    private val habitAnalyzer = DrivingHabitAnalyzer()

    /** SDK 신호(속도/조향각/주행상태)를 운전습관 분석으로 라우팅하는 싱크. Activity가 SDK에 연결한다. */
    val habitSignalListener: DrivingHabitSignalListener = object : DrivingHabitSignalListener {
        override fun onSpeed(metersPerSec: Float) = habitAnalyzer.onSpeed(metersPerSec)
        override fun onSteeringAngle(angleDeg: Float) = habitAnalyzer.onSteeringAngle(angleDeg)
        override fun onDrivingStateChanged(isDriving: Boolean) = handleDrivingStateChange(isDriving)
    }

    // 음성 안내(TTS) 멘트 1회성 이벤트. Activity가 수집해 VoiceGuideClient.speak로 출력한다.
    // 화면이 보유하지 않는 Context 의존(SDK)을 ViewModel에서 분리하기 위한 단방향 이벤트 채널이다.
    private val _voiceEvents = MutableSharedFlow<String>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val voiceEvents: SharedFlow<String> = _voiceEvents.asSharedFlow()

    // 추천 직후 "경유지로 추가할까요?"라고 묻고, 음성 답변을 받기 위해 대상 충전소를 Activity에 알린다.
    // Activity는 안내 TTS가 끝나면 마이크 청취를 시작하고, 긍정 답변이면 터치 없이 경유지를 추가한다.
    private val _confirmWaypointVoice = MutableSharedFlow<HydrogenStation>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val confirmWaypointVoice: SharedFlow<HydrogenStation> = _confirmWaypointVoice.asSharedFlow()

    // Vehicle SDK가 통지한 최신 주행가능거리(km). null이면 아직 통지 전(또는 SDK 비연동 환경).
    // 통지 이후에는 이 값이 차량 상태의 단일 출처가 되어 서버 임시값보다 우선한다.
    @Volatile
    private var sdkRangeKm: Int? = null

    init {
        refresh()
        // 로컬 운전습관 프로파일을 구독해 UI에 반영한다(초기화 버튼 결과도 자동 반영된다).
        viewModelScope.launch {
            habitStore.profile.collect { profile ->
                _uiState.update { it.copy(drivingHabit = profile) }
            }
        }
    }

    /**
     * 주행/정차 상태 변경 시 운전습관 세션을 시작/종료한다.
     * 정차/주차로 세션이 끝나면 집계 결과를 로컬에 누적한다.
     */
    private fun handleDrivingStateChange(isDriving: Boolean) {
        if (isDriving) {
            if (!habitAnalyzer.isActive) habitAnalyzer.startSession()
        } else {
            val session = habitAnalyzer.finishSession() ?: return
            viewModelScope.launch { habitStore.recordSession(session) }
        }
    }

    /** "주행습관 기록 초기화" 버튼. 로컬 누적 기록을 모두 삭제한다(구독으로 UI 자동 갱신). */
    fun resetDrivingHabit() {
        viewModelScope.launch {
            habitStore.reset()
            _voiceEvents.tryEmit("주행 습관 기록을 초기화했어요.")
        }
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
                // SDK 실거리 통지 시엔 주행거리 환산값을 쓴다(서버가 줬던 fuelPercent는 무효화).
                vehicleState = it.vehicleState.copy(vehicleRangeKm = rangeKm, fuelPercent = null),
                fuelMode = mode,
            )
        }

        when (mode) {
            // LOW로 처음 진입했거나 추천 목록이 비어 있을 때만 재요청한다(매 통지마다 호출 방지).
            FuelMode.LOW ->
                if (previousMode != FuelMode.LOW || _uiState.value.stations.isEmpty()) {
                    loadRecommendations()
                }

            FuelMode.SUFFICIENT -> {
                _uiState.update { it.copy(stations = emptyList(), driverMessage = null) }
                // 충분 모드로 (재)진입하거나 아직 대시보드가 없을 때만 불러온다.
                if (previousMode != FuelMode.SUFFICIENT || _uiState.value.dashboard == null) {
                    loadSufficientDashboard()
                }
            }
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
            // 게이지(%)는 VehicleState가 주행가능거리에서 파생한다. SDK엔 수소 탱크/연료 레벨 API가 없고,
            // RANGE_REMAINING은 EV·수소·내연 모두에서 동작하므로 차종 무관하게 일관된 게이지를 준다.
            val vehicleState = baseState.copy(vehicleRangeKm = rangeKm)
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
                _uiState.update { it.copy(stations = emptyList(), driverMessage = null) }
                loadSufficientDashboard()
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
            announceRecommendation(recommendation)
        }
    }

    /** 연료 충분 화면의 서버 드리븐 대시보드를 불러온다. 서버 fuelPercent로 헤더 게이지도 맞춘다. */
    private fun loadSufficientDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 누적 운전습관을 함께 보내 서버가 Gemini로 개인화 인사이트를 만들게 한다.
            val habit = habitStore.snapshot()
            val result = repository.getSufficientDashboard(habit)
            val dashboard = result.dataOrFallback(_uiState.value.dashboard)
            Log.d("HyConnect", "SUFFICIENT dashboard loaded=${dashboard != null}")
            _uiState.update { state ->
                // 서버가 준 연료 잔량(%)을 헤더 게이지의 권위 값으로 사용한다(주행거리 환산 우회).
                val vehicle = dashboard?.vehicle
                val vehicleState = if (vehicle != null) {
                    state.vehicleState.copy(
                        vehicleRangeKm = vehicle.rangeKm,
                        fuelPercent = vehicle.fuelPercent,
                    )
                } else {
                    state.vehicleState
                }
                state.copy(
                    dashboard = dashboard,
                    vehicleState = vehicleState,
                    isLoading = false,
                    errorMessage = result.errorOrNull(),
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
            announceRecommendation(recommendation)
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

    /**
     * 사용자가 추천 목록에서 충전소를 선택(탭)했을 때 호출한다.
     * "~충전소를 경유지로 추가할까요?" 음성 멘트를 내보낸다(경유지 추가 확인 팝업과 함께).
     */
    fun announceStationSelection(station: HydrogenStation) {
        val ment = "${station.name.withJosa("을", "를")} 경유지로 추가할까요?"
        _voiceEvents.tryEmit(ment)
    }

    /** 추천 결과가 채워지면 가장 가까운 충전소를 음성으로 안내한다. (driverMessage intro는 읽지 않는다) */
    private fun announceRecommendation(recommendation: StationRecommendation) {
        val top = recommendation.stations.firstOrNull() ?: return
        val rangeKm = _uiState.value.vehicleState.vehicleRangeKm
        val distance = "%.1f".format(top.distanceKm)
        _voiceEvents.tryEmit(
            "주행가능거리 ${rangeKm}킬로미터 남았어요. 충전이 필요해요. 가장 최적의 충전소는 ${top.name}, ${distance}킬로미터 거리예요. " +
                "경유지로 추가할까요?",
        )
        // 안내가 끝나면 음성 답변(응/아니요)을 받도록 대상 충전소를 Activity에 전달한다.
        _confirmWaypointVoice.tryEmit(top)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun fuelModeFor(remainingRangeKm: Int): FuelMode =
        if (remainingRangeKm <= LOW_RANGE_THRESHOLD_KM) FuelMode.LOW else FuelMode.SUFFICIENT

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: HyConnectRepository,
        private val habitStore: DrivingHabitStore,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // TODO: Hilt/Koin 등 DI를 도입하면 ViewModel 의존성 주입을 프레임워크로 교체한다.
            if (modelClass.isAssignableFrom(HyConnectViewModel::class.java)) {
                return HyConnectViewModel(repository, habitStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        /** 주행가능거리가 이 값(km) 이하면 충전소 추천 모드로 전환한다. */
        const val LOW_RANGE_THRESHOLD_KM = 100

        // 완충 기준거리(차량 최대 연료량 사이즈)는 VehicleState.FULL_RANGE_KM 단일 출처를 따른다.

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
