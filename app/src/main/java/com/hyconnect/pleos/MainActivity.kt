package com.hyconnect.pleos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.ui.HyConnectScreen
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.viewmodel.HyConnectViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HyConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            HyConnectTheme {
                HyConnectScreen(
                    uiState = uiState,
                    onVoiceCallClick = {
                        // TODO: "음성 호출" 버튼을 Gleo AI 또는 SpeechToText SDK와 연결한다.
                        showPrototypeAction("음성 호출 시작")
                    },
                    onSettingsClick = {
                        showPrototypeAction("설정 화면은 프로토타입 범위에서 제외")
                    },
                    onRouteClick = { station ->
                        startRouteGuidance(station)
                    },
                    onMoreStationsClick = {
                        showPrototypeAction("추천 충전소 더보기")
                    },
                )
            }
        }
    }

    private fun startRouteGuidance(station: HydrogenStation) {
        // TODO: "경로 안내" 버튼을 NaviHelper SDK와 연결한다.
        showPrototypeAction("경로 안내 시작: ${station.name}")
    }

    private fun showPrototypeAction(message: String) {
        Log.d("HyConnect", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
