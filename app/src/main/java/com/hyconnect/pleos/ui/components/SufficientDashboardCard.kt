package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.AiInsight
import com.hyconnect.pleos.data.model.DashboardAction
import com.hyconnect.pleos.data.model.DashboardActionStyle
import com.hyconnect.pleos.data.model.DashboardActionType
import com.hyconnect.pleos.data.model.FuelStatus
import com.hyconnect.pleos.data.model.InsightMetric
import com.hyconnect.pleos.data.model.MetricTone
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.data.model.SufficientDashboard
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyPositive
import com.hyconnect.pleos.ui.theme.HyPositiveSoft
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTankRest
import com.hyconnect.pleos.ui.theme.HyTextMuted
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.HyWarn

/**
 * 연료 충분 화면의 서버 드리븐 대시보드.
 * 데이터(AI 인사이트 + 추천 충전소 + 액션)를 받아 카드를 조립한다.
 *
 * 액션 버튼은 서버가 내려준 [SufficientDashboard.actions] 배열대로 그리되,
 * 동작은 타입으로 분기해 [onNavigate]/[onViewMore] 콜백으로 위임한다.
 */
@Composable
fun SufficientDashboardCard(
    dashboard: SufficientDashboard,
    onNavigate: (RecommendedStationCard) -> Unit,
    onViewMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AiInsightCard(
            insight = dashboard.aiInsight,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.25f),
        )
        dashboard.recommendedStation?.let { station ->
            RecommendedStationCardView(
                station = station,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
        DashboardActions(
            actions = dashboard.actions,
            station = dashboard.recommendedStation,
            onNavigate = onNavigate,
            onViewMore = onViewMore,
        )
    }
}

@Composable
private fun AiInsightCard(insight: AiInsight, modifier: Modifier = Modifier) {
    val accent = insight.status.accentColor()
    Column(
        modifier = modifier
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = insight.statusLabel,
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            insight.updatedAt.toHourMinute()?.let { time ->
                Text(text = "$time 업데이트", color = HyTextMuted, fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = insight.subtitle,
            color = HyTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        if (insight.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.message,
                color = HyTextSecondary,
                fontSize = 16.sp,
            )
        }
        // 남는 세로 공간을 흡수해 지표 행을 카드 하단에 정렬한다.
        Spacer(modifier = Modifier.weight(1f))
        if (insight.metrics.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                insight.metrics.forEach { metric ->
                    MetricCell(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricCell(metric: InsightMetric, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(HyTankRest, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = metric.label, color = HyTextMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = metric.value,
                color = metric.tone.valueColor(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            metric.unit?.let { unit ->
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = unit,
                    color = HyTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun RecommendedStationCardView(station: RecommendedStationCard, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            station.badge?.let { badge ->
                Chip(text = badge, textColor = HySurface, background = HyBlue)
                Spacer(modifier = Modifier.size(8.dp))
            }
            if (station.realtimePrice) {
                Chip(text = "실시간 가격", textColor = HyBlue, background = HyBlueSoft)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (station.isOpen) "영업 중" else "영업 종료",
                color = if (station.isOpen) HyPositive else HyTextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = station.name,
            color = HyTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = station.address, color = HyTextSecondary, fontSize = 14.sp)

        // 이름/주소(상단)와 거리·가격 정보(하단) 사이 여백을 흡수해 카드를 꽉 채운다.
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            InfoColumn(label = "거리", value = "%.1fkm".format(station.distanceKm))
            InfoColumn(label = "예상 도착", value = "${station.etaMinutes}분")
            InfoColumn(label = "대기", value = "${station.waitingVehicles}대")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HyBlueSoft, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = "수소 단가", color = HyTextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "%,d원/kg".format(station.pricePerKg),
                    color = HyTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (station.priceDiffFromAvg != 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = station.priceDiffFromAvg.toPriceDiffLabel(),
                        color = if (station.priceDiffFromAvg < 0) HyPositive else HyWarn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "예상 충전 비용", color = HyTextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "%,d원".format(station.estimatedCost),
                    color = HyBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun DashboardActions(
    actions: List<DashboardAction>,
    station: RecommendedStationCard?,
    onNavigate: (RecommendedStationCard) -> Unit,
    onViewMore: () -> Unit,
) {
    if (actions.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        actions.forEach { action ->
            val onClick: () -> Unit = when (action.type) {
                DashboardActionType.NAVIGATE -> {
                    { station?.let(onNavigate) }
                }

                DashboardActionType.VIEW_MORE -> onViewMore
                DashboardActionType.UNKNOWN -> {
                    {}
                }
            }
            // navigate인데 좌표가 없으면 비활성화한다(잘못된 경로 요청 방지).
            val enabled = action.type != DashboardActionType.NAVIGATE ||
                (station?.latitude != null && station.longitude != null)
            ActionButton(
                label = action.label,
                style = action.style,
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    style: DashboardActionStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    when (style) {
        DashboardActionStyle.PRIMARY -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = HyBlue,
                contentColor = HySurface,
            ),
            modifier = modifier.height(56.dp),
        ) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        DashboardActionStyle.SECONDARY -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HyBlue),
            modifier = modifier.height(56.dp),
        ) {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(text = label, color = HyTextMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = HyTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Chip(text: String, textColor: Color, background: Color) {
    Text(
        text = text,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

private fun FuelStatus.accentColor(): Color = when (this) {
    FuelStatus.SUFFICIENT -> HyPositive
    FuelStatus.RECOMMEND -> HyBlue
    FuelStatus.URGENT -> HyWarn
    FuelStatus.UNKNOWN -> HyTextSecondary
}

private fun MetricTone.valueColor(): Color = when (this) {
    MetricTone.POSITIVE -> HyPositive
    MetricTone.WARNING -> HyWarn
    MetricTone.NEUTRAL -> HyTextPrimary
}

/** 평균 대비 가격 차이를 "평균 대비 430원 저렴/비쌈" 문구로 만든다. */
private fun Int.toPriceDiffLabel(): String {
    val abs = kotlin.math.abs(this)
    val suffix = if (this < 0) "저렴" else "비쌈"
    return "평균 대비 %,d원 %s".format(abs, suffix)
}

/** "2026-06-24T14:30:00+09:00" → "14:30". 파싱 실패 시 null. */
private fun String?.toHourMinute(): String? {
    if (this == null) return null
    val time = substringAfter('T', "").take(5)
    return time.takeIf { it.length == 5 && it[2] == ':' }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 980, heightDp = 760)
@Composable
private fun SufficientDashboardCardPreview() {
    com.hyconnect.pleos.ui.theme.HyConnectTheme {
        SufficientDashboardCard(
            dashboard = com.hyconnect.pleos.data.repository.DummyHyConnectData.sufficientDashboard,
            onNavigate = {},
            onViewMore = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}
