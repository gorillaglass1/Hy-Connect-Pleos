package com.x.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.x.myapplication.data.model.RecommendationCard
import com.x.myapplication.data.model.VehicleEnergyInfo
import com.x.myapplication.data.state.RecommendationUiState
import com.x.myapplication.pleos.PleosNaviClient
import com.x.myapplication.pleos.PleosSttClient
import com.x.myapplication.pleos.PleosTtsClient
import com.x.myapplication.pleos.PleosVehicleClient
import com.x.myapplication.screen.RecommendationScreen
import com.x.myapplication.vm.RecommendationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RecommendationViewModel by viewModels()
    private var pleosTtsClient: PleosTtsClient? = null
    private var pleosSttClient: PleosSttClient? = null
    private var pleosNaviClient: PleosNaviClient? = null
    private var pleosVehicleClient: PleosVehicleClient? = null
    private val speechText = mutableStateOf("음성 인식 결과가 여기에 표시됩니다.")
    private val speechStatus = mutableStateOf("음성 인식 대기 중")
    private val ttsStatus = mutableStateOf("TTS 대기 중")
    private val navigationStatus = mutableStateOf("충전소를 누르거나 음성으로 선택하면 경유지로 설정합니다.")
    private val vehicleEnergyInfo = mutableStateOf(VehicleEnergyInfo())
    private val isListening = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pleosTtsClient = runCatching {
            PleosTtsClient(
                context = this,
                clientId = BuildConfig.PLEOS_CLIENT_ID,
                clientSecret = BuildConfig.PLEOS_CLIENT_SECRET,
                callback = object : PleosTtsClient.Callback {
                    override fun onReady() {
                        ttsStatus.value = "TTS 준비 완료"
                    }

                    override fun onStarted() {
                        ttsStatus.value = "TTS 재생 중"
                    }

                    override fun onDone() {
                        ttsStatus.value = "TTS 재생 완료"
                    }

                    override fun onError(message: String) {
                        ttsStatus.value = "TTS 오류: $message"
                    }

                    override fun onServerConnected() {
                        ttsStatus.value = "TTS 서버 연결됨"
                    }

                    override fun onServerFailed(message: String) {
                        ttsStatus.value = "TTS 서버 연결 실패: $message"
                    }
                },
            ).also { it.initialize() }
        }.getOrNull()

        pleosNaviClient = runCatching {
            PleosNaviClient(this).also { it.initialize() }
        }.onFailure {
            navigationStatus.value = "Pleos NaviHelper SDK를 사용할 수 없습니다."
        }.getOrNull()

        pleosVehicleClient = runCatching {
            PleosVehicleClient(this).also { it.initialize() }
        }.onFailure {
            vehicleEnergyInfo.value = VehicleEnergyInfo(status = "Pleos Vehicle SDK를 사용할 수 없습니다.")
        }.getOrNull()

        lifecycleScope.launch {
            pleosVehicleClient?.let { client ->
                vehicleEnergyInfo.value = client.loadEnergyInfo()
            }
        }

        pleosSttClient = runCatching {
            PleosSttClient(
                context = this,
                clientId = BuildConfig.PLEOS_CLIENT_ID,
                clientSecret = BuildConfig.PLEOS_CLIENT_SECRET,
                callback = object : PleosSttClient.Callback {
                    override fun onStatusChanged(message: String) {
                        speechStatus.value = message
                    }

                    override fun onTextUpdated(text: String, completed: Boolean) {
                        speechText.value = text
                        isListening.value = !completed
                        speechStatus.value = if (completed) {
                            "음성 인식 완료"
                        } else {
                            "음성 인식 중입니다."
                        }
                        if (completed) {
                            handleVoiceSelection(text, viewModel.uiState.value)
                        }
                    }

                    override fun onError() {
                        isListening.value = false
                        speechStatus.value = "음성 인식 중 오류가 발생했습니다."
                    }
                },
            ).also { it.initialize() }
        }.onFailure {
            speechStatus.value = "Pleos SpeechToText SDK를 사용할 수 없습니다."
        }.getOrNull()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            RecommendationScreen(
                state = uiState,
                vehicleEnergyInfo = vehicleEnergyInfo.value,
                speechText = speechText.value,
                speechStatus = speechStatus.value,
                ttsStatus = ttsStatus.value,
                navigationStatus = navigationStatus.value,
                isListening = isListening.value,
                onRefreshClick = {
                    viewModel.loadRecommendations()
                },
                onSpeakClick = {
                    val firstCard = (uiState as? RecommendationUiState.Success)?.cards?.firstOrNull()
                    if (firstCard != null) {
                        pleosTtsClient?.speak(
                            "${firstCard.stationName}. 거리 ${firstCard.distanceKm}킬로미터. ${firstCard.reason}"
                        )
                    }
                },
                onStartListeningClick = {
                    speechText.value = ""
                    speechStatus.value = "음성 인식을 시작합니다."
                    isListening.value = true
                    runCatching {
                        pleosSttClient?.startRecognition()
                    }.onFailure {
                        isListening.value = false
                        speechStatus.value = "음성 인식을 시작하지 못했습니다."
                    }
                },
                onStopListeningClick = {
                    runCatching {
                        pleosSttClient?.stopRecognition()
                    }
                    isListening.value = false
                    speechStatus.value = "음성 인식을 중지했습니다."
                },
                onStationClick = { card ->
                    requestWaypoint(card)
                },
            )
        }

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                delay(1_500L)
                pleosTtsClient?.speak("안녕하세요. 하이커넥트입니다. 차량 상태에 맞춰 충전소를 자동으로 추천해 드릴게요.")
            }
        }
    }

    override fun onDestroy() {
        runCatching { pleosVehicleClient?.release() }
        pleosVehicleClient = null
        runCatching { pleosNaviClient?.release() }
        pleosNaviClient = null
        runCatching { pleosSttClient?.release() }
        pleosSttClient = null
        runCatching { pleosTtsClient?.release() }
        pleosTtsClient = null
        super.onDestroy()
    }

    private fun handleVoiceSelection(text: String, state: RecommendationUiState) {
        val cards = (state as? RecommendationUiState.Success)?.cards.orEmpty()
        val selected = selectCardFromVoice(text, cards)
        if (selected == null) {
            navigationStatus.value = "음성에서 선택할 충전소를 찾지 못했습니다."
            pleosTtsClient?.speak("선택할 충전소를 찾지 못했습니다. 충전소 이름이나 첫 번째처럼 말해주세요.")
            return
        }
        requestWaypoint(selected)
    }

    private fun selectCardFromVoice(text: String, cards: List<RecommendationCard>): RecommendationCard? {
        val normalized = text.replace(" ", "")
        val ordinalIndex = when {
            "첫" in normalized || "1" in normalized || "일번" in normalized -> 0
            "둘" in normalized || "두번째" in normalized || "2" in normalized || "이번" in normalized -> 1
            "셋" in normalized || "세번째" in normalized || "3" in normalized || "삼번" in normalized -> 2
            else -> null
        }
        if (ordinalIndex != null) return cards.getOrNull(ordinalIndex)

        return cards.firstOrNull { card ->
            val stationName = card.stationName.replace(" ", "")
            stationName in normalized || normalized in stationName
        }
    }

    private fun requestWaypoint(card: RecommendationCard) {
        val result = pleosNaviClient?.addWaypoint(card)
            ?: Result.failure(IllegalStateException("Pleos NaviHelper SDK를 사용할 수 없습니다."))

        result.onSuccess {
            navigationStatus.value = "${card.stationName}을 경유지로 설정했습니다."
            pleosTtsClient?.speak("${card.stationName}을 경유지로 설정했습니다.")
        }.onFailure { error ->
            navigationStatus.value = "${card.stationName} 경유지 설정 실패: ${error.message}"
            pleosTtsClient?.speak("경유지 설정에 실패했습니다.")
        }
    }

}
