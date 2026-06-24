package com.hyconnect.pleos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.hyconnect.pleos.ui.components.DashboardPlaceholder
import com.hyconnect.pleos.ui.components.HydrogenTankCard
import com.hyconnect.pleos.ui.components.LowFuelBanner
import com.hyconnect.pleos.ui.components.NlQueryBar
import com.hyconnect.pleos.ui.components.StationListCard
import com.hyconnect.pleos.ui.components.SufficientDashboardCard
import com.hyconnect.pleos.ui.components.WaypointConfirmDialog
import com.hyconnect.pleos.ui.theme.HyBackground
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
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
        Column(
            modifier = Modifier
                .width(980.dp)
                .fillMaxHeight()
                .background(HySurface, RoundedCornerShape(24.dp))
                .border(1.dp, HyBorder, RoundedCornerShape(24.dp))
                .padding(24.dp),
        ) {
            Header()
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

                FuelMode.SUFFICIENT -> {
                    val dashboard = uiState.dashboard
                    if (dashboard != null) {
                        SufficientDashboardCard(
                            dashboard = dashboard,
                            onNavigate = onDashboardNavigate,
                            onViewMore = onMoreStationsClick,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        // 대시보드 로드 전(또는 데이터 없음): 기존 플레이스홀더로 폴백.
                        DashboardPlaceholder(modifier = Modifier.weight(1f))
                    }
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
    NlQueryBar(
        query = uiState.nlQuery,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        onVoiceClick = onVoiceClick,
    )
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
private fun Header() {
    Text(
        text = "HyConnect",
        color = HyBlue,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
    )
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
