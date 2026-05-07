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
                onRouteClick = onRouteClick,
                onMoreClick = onMoreStationsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Header(
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
                text = "수소 충전의 더 스마트한 파트너입니다.",
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
                text = "⚙",
                color = HyTextPrimary,
                fontSize = 24.sp,
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
                vehicleState = VehicleState(),
                aiRecommendation = AiRecommendation(),
                stations = listOf(
                    HydrogenStation(
                        id = "hyundai-yangjae",
                        name = "현대 수소충전소 양재",
                        address = "경기 성남시 분당구 문로 12 길 123",
                        status = "운영 중",
                        pressureInfo = "700bar 사용 가능",
                        distanceKm = 2.1,
                        waitMinutes = 5,
                        isRecommended = true,
                    ),
                    HydrogenStation(
                        id = "gaia",
                        name = "가이아 수소충전소",
                        address = "경기 성남시 분당구 구룡로 289",
                        status = "운영 중",
                        pressureInfo = "700bar 사용 가능",
                        distanceKm = 4.5,
                        waitMinutes = 10,
                    ),
                    HydrogenStation(
                        id = "hyundai-pangyo",
                        name = "현대 수소충전소 판교",
                        address = "경기 성남시 분당구 고등로 98",
                        status = "운영 중",
                        pressureInfo = "700bar 사용 가능",
                        distanceKm = 8.7,
                        waitMinutes = 15,
                    ),
                ),
            ),
            onVoiceCallClick = {},
            onSettingsClick = {},
            onRouteClick = {},
            onMoreStationsClick = {},
        )
    }
}
