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
import com.hyconnect.pleos.navigation.NavigationResult
import com.hyconnect.pleos.navigation.PleosNaviHelperNavigationClient
import com.hyconnect.pleos.ui.HyConnectScreen
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.vehicle.RangeRemainingTrigger
import com.hyconnect.pleos.vehicle.VehicleSdkClient
import com.hyconnect.pleos.viewmodel.HyConnectViewModel
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val navigationClient: PleosNaviHelperNavigationClient by lazy {
        PleosNaviHelperNavigationClient(this)
    }

    private val vehicleSdkClient: VehicleSdkClient by lazy {
        VehicleSdkClient(this)
    }

    private val viewModel: HyConnectViewModel by viewModels {
        HyConnectViewModel.Factory((application as HyConnectApplication).repository)
    }

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
        vehicleSdkClient.registerListeners(rangeTrigger)
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

            HyConnectTheme {
                HyConnectScreen(
                    uiState = uiState,
                    onVoiceCallClick = {
                        // TODO: 추후 Gleo AI SDK SpeechToText로 교체.
                        showPrototypeAction("음성 호출은 프로토타입 Toast로 동작합니다.")
                    },
                    onQueryChange = viewModel::updateNlQuery,
                    onSearch = viewModel::searchStations,
                    onAddWaypoint = { station ->
                        addWaypoint(station)
                    },
                    onMoreStationsClick = {
                        showPrototypeAction("추천 충전소 더보기는 다음 단계에서 연결합니다.")
                    },
                    onRefreshClick = viewModel::refresh,
                )
            }
        }
    }

    override fun onDestroy() {
        navigationClient.release()
        vehicleSdkClient.unregisterListeners()
        vehicleSdkClient.release()
        super.onDestroy()
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

    private fun showPrototypeAction(message: String) {
        Log.d("HyConnect", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
