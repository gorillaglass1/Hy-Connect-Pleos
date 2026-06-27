package com.hyconnect.pleos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.navigation.NavigationResult
import com.hyconnect.pleos.navigation.PleosNaviHelperNavigationClient
import com.hyconnect.pleos.ui.HyConnectScreen
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.vehicle.RangeRemainingTrigger
import com.hyconnect.pleos.vehicle.VehicleSdkClient
import com.hyconnect.pleos.viewmodel.HyConnectViewModel
import com.hyconnect.pleos.voice.VoiceGuideClient
import com.hyconnect.pleos.voice.VoiceInputClient
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val navigationClient: PleosNaviHelperNavigationClient by lazy {
        PleosNaviHelperNavigationClient(this)
    }

    private val vehicleSdkClient: VehicleSdkClient by lazy {
        VehicleSdkClient(this)
    }

    // 음성 안내(TTS)·음성 입력(STT). 정책상 OnDevice 모드만 사용한다.
    private val voiceGuideClient: VoiceGuideClient by lazy {
        VoiceGuideClient(this)
    }

    private val voiceInputClient: VoiceInputClient by lazy {
        VoiceInputClient(this)
    }

    private val viewModel: HyConnectViewModel by viewModels {
        HyConnectViewModel.Factory(
            (application as HyConnectApplication).repository,
            (application as HyConnectApplication).drivingHabitStore,
        )
    }

    // 음성 추천 확인 대기 중인 충전소. 안내 종료 후 마이크로 받은 답변을 이 충전소에 적용한다.
    // null이면 일반 음성 입력(검색)으로 처리한다.
    @Volatile
    private var pendingVoiceStation: HydrogenStation? = null

    // Vehicle SDK에서 받은 주행가능거리/주행상태를 정책에 따라 ViewModel로 전달한다.
    private val rangeTrigger: RangeRemainingTrigger by lazy {
        RangeRemainingTrigger(
            lowRangeThresholdKm = HyConnectViewModel.LOW_RANGE_THRESHOLD_KM,
            // 주행 여부와 무관하게 모든 통지를 ViewModel로 보낸다.
            // 임계값 이하이면 ViewModel이 즉시 LOW(충전소 추천 화면)로 전환한다.
            onRangeUpdated = { rangeKm, _ -> viewModel.onVehicleRangeUpdated(rangeKm.roundToInt()) },
            // 화면 전환은 onRangeUpdated가 담당하므로 아래 두 콜백은 로그/음성 안내 슬롯으로만 둔다.
            onDrivingAndLow = { rangeKm -> Log.d("HyConnect", "주행 중 충전 필요: ${rangeKm}km") },
            onStoppedAndLow = { rangeKm -> Log.d("HyConnect", "정차 중 충전 필요: ${rangeKm}km") },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationClient.initialize()
        vehicleSdkClient.initialize()
        // 운전습관 신호(속도/조향각/주행상태)도 함께 구독해 ViewModel의 분석기로 전달한다.
        vehicleSdkClient.registerListeners(rangeTrigger, viewModel.habitSignalListener)
        voiceGuideClient.initialize()
        voiceInputClient.initialize()
        // 2단계 대화형 경유지 추가: 추천 안내가 끝나면 마이크를 열어 응/아니요를 받는다.
        voiceGuideClient.onUtteranceDone = {
            // "경유지로 추가할까요?" 안내가 끝났고 대기 중인 충전소가 있으면 답변 청취를 시작한다.
            if (pendingVoiceStation != null) {
                voiceInputClient.startListening()
            }
        }
        voiceInputClient.onFinalText = { spoken ->
            val pending = pendingVoiceStation
            if (pending != null) {
                // 추천 확인에 대한 답변 처리. 다음 안내(onUtteranceDone)가 또 청취를 열지 않도록 먼저 비운다.
                pendingVoiceStation = null
                if (isAffirmative(spoken)) {
                    addWaypoint(pending)
                } else {
                    voiceGuideClient.speak("알겠습니다. 추가하지 않을게요.")
                }
            } else {
                // 일반 음성 입력은 그대로 자연어 충전소 검색에 넣는다.
                showPrototypeAction("\"$spoken\" 로 충전소를 찾을게요.")
                viewModel.searchStations(spoken)
            }
        }
        voiceInputClient.onListeningChanged = { listening ->
            if (listening) showPrototypeAction("듣고 있어요. 말씀해 주세요.")
        }
        voiceInputClient.onError = {
            pendingVoiceStation = null
            showPrototypeAction("음성 인식에 실패했습니다.")
        }
        // Navi 앱이 비동기로 통지하는 경유지 추가 결과/오류를 사용자에게 보여준다.
        navigationClient.onNaviEvent = { result ->
            when (result) {
                is NavigationResult.WaypointAdded -> showPrototypeAction("경유지가 추가되었습니다.")
                is NavigationResult.Started -> showPrototypeAction("경로 안내를 시작합니다.")
                is NavigationResult.Failed -> {
                    result.cause?.let { Log.w("HyConnect", result.message, it) }
                    showPrototypeAction(result.message)
                }
            }
        }

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }

            // 추천/선택 시 ViewModel이 내보낸 멘트를 OnDevice TTS로 출력한다.
            LaunchedEffect(Unit) {
                viewModel.voiceEvents.collect { ment -> voiceGuideClient.speak(ment) }
            }

            // 추천 직후 "경유지로 추가할까요?" 안내 대상 충전소를 받아둔다(안내 종료 후 음성 답변 청취).
            LaunchedEffect(Unit) {
                viewModel.confirmWaypointVoice.collect { station -> pendingVoiceStation = station }
            }

            HyConnectTheme {
                HyConnectScreen(
                    uiState = uiState,
                    onVoiceCallClick = {
                        // Gleo AI SpeechToText(OnDevice)로 마이크 입력을 받는다.
                        voiceInputClient.startListening()
                    },
                    onQueryChange = viewModel::updateNlQuery,
                    onSearch = viewModel::searchStations,
                    onStationSelected = viewModel::announceStationSelection,
                    onAddWaypoint = { station ->
                        addWaypoint(station)
                    },
                    onMoreStationsClick = {
                        showPrototypeAction("추천 충전소 더보기는 다음 단계에서 연결합니다.")
                    },
                    onRefreshClick = viewModel::refresh,
                    onDashboardNavigate = { card -> addWaypoint(card.toHydrogenStation()) },
                    onResetHabit = viewModel::resetDrivingHabit,
                )
            }
        }
    }

    override fun onDestroy() {
        navigationClient.release()
        vehicleSdkClient.unregisterListeners()
        vehicleSdkClient.release()
        voiceGuideClient.release()
        voiceInputClient.release()
        super.onDestroy()
    }

    /** 음성 답변이 긍정(응/네/추가해줘 등)인지 판단한다. */
    private fun isAffirmative(text: String): Boolean {
        val t = text.replace(" ", "")
        if (listOf("아니", "안돼", "안 돼", "취소", "싫", "괜찮").any { it.replace(" ", "") in t }) return false
        return listOf("응", "네", "예", "어", "그래", "좋아", "추가", "해줘", "웅", "오케", "ok", "당연", "그러", "가자")
            .any { it in t }
    }

    private fun addWaypoint(station: HydrogenStation) {
        // 확인 팝업에서 '경유지 추가'를 누른 뒤 호출된다. 선호 학습도 함께 전송한다.
        viewModel.selectStationForRoute(station)
        // addWaypoint는 비동기 전송이다. 여기서는 전송/검증 결과만 알리고,
        // 최종 추가 성공 여부는 navigationClient.onNaviEvent 콜백에서 통지된다.
        when (val result = navigationClient.addWaypoint(station)) {
            is NavigationResult.WaypointAdded -> showPrototypeAction("${result.stationName} 경유지 추가를 요청했습니다.")
            is NavigationResult.Started -> showPrototypeAction("${result.stationName}까지 경로 안내를 시작합니다.")
            is NavigationResult.Failed -> {
                // 전송 전 검증 실패(좌표 오류 등). 잘못된 요청을 보내지 않고 즉시 사유를 안내한다.
                result.cause?.let { Log.w("HyConnect", result.message, it) }
                showPrototypeAction(result.message)
            }
        }
    }

    /** 대시보드 추천 충전소 카드를 내비 경로 안내에 쓸 수 있는 모델로 변환한다. */
    private fun RecommendedStationCard.toHydrogenStation(): HydrogenStation =
        HydrogenStation(
            id = stationId,
            name = name,
            address = address,
            status = if (isOpen) "영업 중" else "영업 종료",
            pressureInfo = "",
            distanceKm = distanceKm,
            waitMinutes = etaMinutes,
            isRecommended = true,
            latitude = latitude,
            longitude = longitude,
        )

    private fun showPrototypeAction(message: String) {
        Log.d("HyConnect", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
