package com.x.myapplication.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x.myapplication.data.model.RecommendationCard
import com.x.myapplication.data.model.VehicleEnergyInfo
import com.x.myapplication.data.state.RecommendationUiState

private val PleosInk = Color(0xFF17201C)
private val PleosBackground = Color(0xFFF7FAF8)
private val PleosPanel = Color(0xFFFFFFFF)
private val PleosPanelSoft = Color(0xFFF0F6F2)
private val PleosGreen = Color(0xFF3DDC84)
private val PleosGreenDeep = Color(0xFF0B8F53)
private val PleosBlue = Color(0xFF2F6FFF)
private val PleosText = Color(0xFF17201C)
private val PleosSubText = Color(0xFF66746D)

@Composable
fun RecommendationScreen(
    state: RecommendationUiState,
    vehicleEnergyInfo: VehicleEnergyInfo,
    speechText: String,
    speechStatus: String,
    ttsStatus: String,
    navigationStatus: String,
    isListening: Boolean,
    onRefreshClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onStartListeningClick: () -> Unit,
    onStopListeningClick: () -> Unit,
    onStationClick: (RecommendationCard) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), PleosBackground, Color(0xFFEAF7EF))
                )
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            VehicleEnergyPanel(vehicleEnergyInfo)

            Spacer(modifier = Modifier.height(14.dp))

            ControlPanel(
                speechText = speechText,
                speechStatus = speechStatus,
                ttsStatus = ttsStatus,
                navigationStatus = navigationStatus,
                isListening = isListening,
                onRefreshClick = onRefreshClick,
                onSpeakClick = onSpeakClick,
                onStartListeningClick = onStartListeningClick,
                onStopListeningClick = onStopListeningClick,
                canSpeak = state is RecommendationUiState.Success,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "추천 충전소",
                color = PleosText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (state) {
                is RecommendationUiState.Loading -> Text("추천 정보를 불러오는 중입니다.", color = PleosSubText)
                is RecommendationUiState.Error -> Text("에러 발생: ${state.message}", color = Color(0xFFD84432))
                is RecommendationUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(state.cards) { index, card ->
                            RecommendationItem(
                                index = index + 1,
                                card = card,
                                onClick = { onStationClick(card) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleEnergyPanel(info: VehicleEnergyInfo) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF2FFF7), Color(0xFFE2F8EA))
                    )
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("PLEOS ENERGY", color = PleosGreenDeep, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("현재 차량 에너지", color = PleosText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(info.status, color = PleosSubText, fontSize = 13.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricTile("배터리", info.batteryPercent?.let { "$it%" } ?: "--", Modifier.weight(1f))
                MetricTile("주행가능", info.rangeKm?.let { "%.0f km".format(it) } ?: "--", Modifier.weight(1f))
                MetricTile("에너지", info.chargingState ?: info.efficiency ?: "--", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x1F3DDC84))
            .padding(14.dp)
    ) {
        Text(label, color = PleosSubText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, color = PleosText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ControlPanel(
    speechText: String,
    speechStatus: String,
    ttsStatus: String,
    navigationStatus: String,
    isListening: Boolean,
    onRefreshClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onStartListeningClick: () -> Unit,
    onStopListeningClick: () -> Unit,
    canSpeak: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PleosPanel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("음성/네비게이션", color = PleosText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(speechStatus, color = PleosSubText, fontSize = 13.sp)
            Text(ttsStatus, color = PleosSubText, fontSize = 13.sp)
            Text(speechText.ifBlank { "충전소 이름 또는 첫 번째, 두 번째처럼 말하면 경유지로 설정합니다." }, color = PleosText)
            Spacer(modifier = Modifier.height(6.dp))
            Text(navigationStatus, color = PleosBlue, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PleosButton("새로고침", onRefreshClick, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                PleosButton("음성 안내", onSpeakClick, Modifier.weight(1f), enabled = canSpeak)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PleosButton("음성 선택", onStartListeningClick, Modifier.weight(1f), enabled = !isListening)
                Spacer(modifier = Modifier.width(8.dp))
                PleosButton("중지", onStopListeningClick, Modifier.weight(1f), enabled = isListening)
            }
        }
    }
}

@Composable
private fun PleosButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PleosGreen,
            contentColor = PleosInk,
            disabledContainerColor = PleosPanelSoft,
            disabledContentColor = PleosSubText,
        ),
        modifier = modifier
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecommendationItem(
    index: Int,
    card: RecommendationCard,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PleosPanel),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$index. ${card.stationName}", color = PleosText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("%.1f km".format(card.distanceKm), color = PleosGreenDeep, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(if (card.isReachable) "도달 가능" else "도달 어려움", color = PleosBlue, fontSize = 13.sp)
            Text("추천 이유: ${card.reason}", color = PleosText)
            card.address?.let { Text(it, color = PleosSubText, fontSize = 13.sp) }
            card.caution?.let { Text("주의: $it", color = Color(0xFFD65A43), fontSize = 13.sp) }
        }
    }
}
