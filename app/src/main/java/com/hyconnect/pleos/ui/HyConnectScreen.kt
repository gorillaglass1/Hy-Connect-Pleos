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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.ui.components.AiRecommendationCard
import com.hyconnect.pleos.ui.components.HydrogenTankCard
import com.hyconnect.pleos.ui.components.StationListCard
import com.hyconnect.pleos.ui.components.VoiceCallButton
import com.hyconnect.pleos.ui.theme.HyBackground
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.viewmodel.HyConnectUiState

@Composable
fun HyConnectScreen(
    uiState: HyConnectUiState,
    onVoiceCallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRouteClick: (HydrogenStation) -> Unit,
    onMoreStationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            AiRecommendationCard(recommendation = uiState.aiRecommendation)
            Spacer(modifier = Modifier.height(14.dp))
            StationListCard(
                stations = uiState.stations,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onRouteClick = onRouteClick,
                onMoreClick = onMoreStationsClick,
                onRefreshClick = onRefreshClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
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
private fun HyConnectScreenPreview() {
    HyConnectTheme {
        HyConnectScreen(
            uiState = HyConnectUiState(
                vehicleState = VehicleState(
                    hydrogenPercent = 78,
                    vehicleRangeKm = 500,
                    message = "수소 충전량이 충분합니다.",
                ),
                aiRecommendation = AiRecommendation(
                    title = "지금 충전하기 좋은 타이밍입니다.",
                    dustSummary = "현재 미세먼지 21ug/m3, 외부 활동 부담이 낮습니다.",
                    routeSummary = "현 경로 기준 대기 시간이 가장 짧은 충전소가 있습니다.",
                ),
                stations = previewStations,
                isLoading = false,
            ),
            onVoiceCallClick = {},
            onSettingsClick = {},
            onRouteClick = {},
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
    ),
    HydrogenStation(
        id = "gaia",
        name = "가이아 수소충전소",
        address = "경기 성남시 분당구 구미로 289",
        status = "운영 중",
        pressureInfo = "700bar 사용 가능",
        distanceKm = 4.5,
        waitMinutes = 10,
    ),
)
