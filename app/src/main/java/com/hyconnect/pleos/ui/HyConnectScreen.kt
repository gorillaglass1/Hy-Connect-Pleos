package com.hyconnect.pleos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.data.repository.DummyHyConnectData
import com.hyconnect.pleos.ui.components.DrivingHabitDashboard
import com.hyconnect.pleos.ui.components.HydrogenTankCard
import com.hyconnect.pleos.ui.components.LowFuelBanner
import com.hyconnect.pleos.ui.components.NlQueryBar
import com.hyconnect.pleos.ui.components.PleosGlow
import com.hyconnect.pleos.ui.components.StationListCard
import com.hyconnect.pleos.ui.components.WaypointConfirmDialog
import com.hyconnect.pleos.ui.theme.HyBackground
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyWarnSoft
import com.hyconnect.pleos.ui.theme.hyCard
import com.hyconnect.pleos.viewmodel.FuelMode
import com.hyconnect.pleos.viewmodel.HyConnectUiState

@Composable
fun HyConnectScreen(
    uiState: HyConnectUiState,
    onVoiceCallClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onStationSelected: (HydrogenStation) -> Unit,
    onAddWaypoint: (HydrogenStation) -> Unit,
    onMoreStationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onDashboardNavigate: (RecommendedStationCard) -> Unit,
    onResetHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 경유지로 추가하기 전 확인 팝업 대상. null이면 팝업을 숨긴다.
    var pendingStation by remember { mutableStateOf<HydrogenStation?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HyBackground)
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .width(980.dp)
                .fillMaxHeight()
                .hyCard(corner = 28.dp),
        ) {
            // 상태별 PLEOS 그라데이션: 연료 부족이면 오렌지, 충분하면 블루 틴트가 하단에서 피어오른다.
            PleosGlow(
                color = if (uiState.fuelMode == FuelMode.LOW) HyWarnSoft else HyBlueSoft,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(260.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Toolbar()
                Spacer(modifier = Modifier.height(16.dp))
                HydrogenTankCard(vehicleState = uiState.vehicleState)
                Spacer(modifier = Modifier.height(14.dp))

                when (uiState.fuelMode) {
                FuelMode.LOW -> LowFuelContent(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onVoiceClick = onVoiceCallClick,
                    onStationSelect = {
                        // 선택 시 음성 안내("~경유지로 추가할까요?")를 내보내고 확인 팝업을 띄운다.
                        onStationSelected(it)
                        pendingStation = it
                    },
                    onMoreStationsClick = onMoreStationsClick,
                    onRefreshClick = onRefreshClick,
                )

                FuelMode.SUFFICIENT -> DrivingHabitDashboard(
                    // 상단: 안전운전 앱 스타일 주행 습관 점수 패널(+기록 초기화)
                    habit = uiState.drivingHabit,
                    // 하단: Gemini 개인화 충전 인사이트 + 추천 충전소(로드 전이면 플레이스홀더)
                    dashboard = uiState.dashboard,
                    onNavigate = onDashboardNavigate,
                    onResetHabit = onResetHabit,
                    modifier = Modifier.weight(1f),
                )
                }
            }
        }
    }

    pendingStation?.let { station ->
        WaypointConfirmDialog(
            station = station,
            onConfirm = {
                onAddWaypoint(station)
                pendingStation = null
            },
            onDismiss = { pendingStation = null },
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.LowFuelContent(
    uiState: HyConnectUiState,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onStationSelect: (HydrogenStation) -> Unit,
    onMoreStationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    LowFuelBanner(
        rangeKm = uiState.vehicleState.vehicleRangeKm,
        driverMessage = uiState.driverMessage,
    )
    Spacer(modifier = Modifier.height(14.dp))
//    NlQueryBar(
//        query = uiState.nlQuery,
//        onQueryChange = onQueryChange,
//        onSearch = onSearch,
//        onVoiceClick = onVoiceClick,
//    )
    Spacer(modifier = Modifier.height(14.dp))
    StationListCard(
        stations = uiState.stations,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onRouteClick = onStationSelect,
        onMoreClick = onMoreStationsClick,
        onRefreshClick = onRefreshClick,
        actionLabel = "경유지 추가",
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun Toolbar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 샘플 VehicleToolbar의 아이콘+타이틀 구성을 차용한 로고 칩.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(HyBlue, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "H",
                color = HySurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.W800,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "HyConnect",
            color = HyTextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.W700,
        )
    }
}

@Preview(showBackground = true, widthDp = 1180, heightDp = 840)
@Composable
private fun HyConnectScreenLowPreview() {
    HyConnectTheme {
        HyConnectScreen(
            uiState = HyConnectUiState(
                vehicleState = VehicleState(
                    vehicleRangeKm = 92,
                    message = "충전이 필요합니다.",
                ),
                fuelMode = FuelMode.LOW,
                stations = previewStations,
                nlQuery = "제일 가까운 충전소 추천해줘",
                driverMessage = "경로에서 가장 가까운 충전소를 골라봤어요.",
                isLoading = false,
            ),
            onVoiceCallClick = {},
            onQueryChange = {},
            onSearch = {},
            onStationSelected = {},
            onAddWaypoint = {},
            onMoreStationsClick = {},
            onRefreshClick = {},
            onDashboardNavigate = {},
            onResetHabit = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 1180, heightDp = 840)
@Composable
private fun HyConnectScreenSufficientPreview() {
    HyConnectTheme {
        HyConnectScreen(
            uiState = HyConnectUiState(
                vehicleState = VehicleState(
                    vehicleRangeKm = 500,
                    fuelPercent = 83,
                    message = "수소 충전량이 충분합니다.",
                ),
                fuelMode = FuelMode.SUFFICIENT,
                dashboard = DummyHyConnectData.sufficientDashboard,
                drivingHabit = DummyHyConnectData.drivingHabit,
                isLoading = false,
            ),
            onVoiceCallClick = {},
            onQueryChange = {},
            onSearch = {},
            onStationSelected = {},
            onAddWaypoint = {},
            onMoreStationsClick = {},
            onRefreshClick = {},
            onDashboardNavigate = {},
            onResetHabit = {},
        )
    }
}

private val previewStations = listOf(
    HydrogenStation(
        id = "hyundai-yangjae",
        name = "현대 수소충전소 양재",
        address = "서울 서초구 바우뫼로 12길 123",
        status = "운영 중",
        pressureInfo = "700bar 사용 가능",
        distanceKm = 2.1,
        waitMinutes = 5,
        isRecommended = true,
        latitude = 37.468164,
        longitude = 127.038703,
    ),
    HydrogenStation(
        id = "gaia",
        name = "가이아 수소충전소",
        address = "경기 성남시 분당구 구미로 289",
        status = "운영 중",
        pressureInfo = "700bar 사용 가능",
        distanceKm = 4.5,
        waitMinutes = 10,
        latitude = 37.412,
        longitude = 127.131,
    ),
)
