package com.hyconnect.pleos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.ui.components.DashboardPlaceholder
import com.hyconnect.pleos.ui.components.HydrogenTankCard
import com.hyconnect.pleos.ui.components.LowFuelBanner
import com.hyconnect.pleos.ui.components.NlQueryBar
import com.hyconnect.pleos.ui.components.StationListCard
import com.hyconnect.pleos.ui.components.VoiceCallButton
import com.hyconnect.pleos.ui.components.WaypointConfirmDialog
import com.hyconnect.pleos.ui.theme.HyBackground
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.viewmodel.FuelMode
import com.hyconnect.pleos.viewmodel.HyConnectUiState

@Composable
fun HyConnectScreen(
    uiState: HyConnectUiState,
    onVoiceCallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onAddWaypoint: (HydrogenStation) -> Unit,
    onMoreStationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
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
            Header(
                isLoading = uiState.isLoading,
                onVoiceCallClick = onVoiceCallClick,
                onSettingsClick = onSettingsClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HydrogenTankCard(vehicleState = uiState.vehicleState)
            Spacer(modifier = Modifier.height(14.dp))

            when (uiState.fuelMode) {
                FuelMode.LOW -> LowFuelContent(
                    uiState = uiState,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    onVoiceClick = onVoiceCallClick,
                    onStationSelect = { pendingStation = it },
                    onMoreStationsClick = onMoreStationsClick,
                    onRefreshClick = onRefreshClick,
                )

                FuelMode.SUFFICIENT -> DashboardPlaceholder(
                    modifier = Modifier.weight(1f),
                )
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
private fun Header(
    isLoading: Boolean,
    onVoiceCallClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "HyConnect",
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "하이커넥트",
                color = HyTextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isLoading) "차량과 충전소 데이터를 동기화하고 있습니다." else "수소 충전까지 스마트한 파트너입니다.",
                color = HyTextSecondary,
                fontSize = 17.sp,
            )
        }

        VoiceCallButton(onClick = onVoiceCallClick)
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(52.dp)
                .background(HyBackground, CircleShape)
                .border(1.dp, HyBorder, CircleShape),
        ) {
            Text(
                text = "설정",
                color = HyTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1180, heightDp = 840)
@Composable
private fun HyConnectScreenLowPreview() {
    HyConnectTheme {
        HyConnectScreen(
            uiState = HyConnectUiState(
                vehicleState = VehicleState(
                    hydrogenPercent = 18,
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
            onSettingsClick = {},
            onQueryChange = {},
            onSearch = {},
            onAddWaypoint = {},
            onMoreStationsClick = {},
            onRefreshClick = {},
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
                    hydrogenPercent = 78,
                    vehicleRangeKm = 500,
                    message = "수소 충전량이 충분합니다.",
                ),
                fuelMode = FuelMode.SUFFICIENT,
                isLoading = false,
            ),
            onVoiceCallClick = {},
            onSettingsClick = {},
            onQueryChange = {},
            onSearch = {},
            onAddWaypoint = {},
            onMoreStationsClick = {},
            onRefreshClick = {},
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
