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
import com.hyconnect.pleos.navigation.AndroidGeoNavigationClient
import com.hyconnect.pleos.navigation.NavigationClient
import com.hyconnect.pleos.navigation.NavigationResult
import com.hyconnect.pleos.ui.HyConnectScreen
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.viewmodel.HyConnectViewModel

class MainActivity : ComponentActivity() {
    private val navigationClient: NavigationClient by lazy {
        AndroidGeoNavigationClient(this)
    }

    private val viewModel: HyConnectViewModel by viewModels {
        HyConnectViewModel.Factory((application as HyConnectApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }

            HyConnectTheme {
                HyConnectScreen(
                    uiState = uiState,
                    onVoiceCallClick = {
                        // TODO: 추후 Gleo AI SDK SpeechToText로 교체.
                        showPrototypeAction("음성 호출은 프로토타입 Toast로 동작합니다.")
                    },
                    onSettingsClick = {
                        showPrototypeAction("설정 화면은 프로토타입 범위에서 제외되었습니다.")
                    },
                    onRouteClick = { station ->
                        startRouteGuidance(station)
                    },
                    onMoreStationsClick = {
                        showPrototypeAction("추천 충전소 더보기는 다음 단계에서 연결합니다.")
                    },
                    onRefreshClick = viewModel::refresh,
                )
            }
        }
    }

    private fun startRouteGuidance(station: HydrogenStation) {
        // TODO: 추후 Pleos NaviHelper SDK로 교체.
        when (val result = navigationClient.startRouteGuidance(station)) {
            is NavigationResult.Started -> showPrototypeAction("경로 안내 시작: ${result.stationName}")
            is NavigationResult.Failed -> {
                result.cause?.let { Log.w("HyConnect", result.message, it) }
                showPrototypeAction(result.message)
            }
        }
    }

    private fun showPrototypeAction(message: String) {
        Log.d("HyConnect", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
