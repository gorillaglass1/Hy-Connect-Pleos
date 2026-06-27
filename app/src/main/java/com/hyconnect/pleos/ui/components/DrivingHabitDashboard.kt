package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.RecommendedStationCard
import com.hyconnect.pleos.data.model.SufficientDashboard
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyPositive
import com.hyconnect.pleos.ui.theme.HyPositiveSoft
import com.hyconnect.pleos.ui.theme.HyTankRest
import com.hyconnect.pleos.ui.theme.HyTextMuted
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.HyWarn
import com.hyconnect.pleos.ui.theme.HyWarnSoft
import com.hyconnect.pleos.ui.theme.hyCard
import com.hyconnect.pleos.vehicle.habit.DrivingHabitProfile
import com.hyconnect.pleos.vehicle.habit.DrivingStyle

/**
 * 연료 충분 화면.
 * 상단: 샘플 안전운전 앱 스타일의 "주행 습관 점수" 패널(로컬 누적 운전습관 시각화 + 기록 초기화).
 * 하단: Gemini 개인화 충전 인사이트 + 추천 충전소 카드([SufficientDashboardCard]).
 */
@Composable
fun DrivingHabitDashboard(
    habit: DrivingHabitProfile,
    dashboard: SufficientDashboard?,
    onNavigate: (RecommendedStationCard) -> Unit,
    onResetHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DrivingScorePanel(
            habit = habit,
            onResetHabit = onResetHabit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        if (dashboard != null) {
            SufficientDashboardCard(
                dashboard = dashboard,
                onNavigate = onNavigate,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.4f),
            )
        } else {
            DashboardPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.4f),
            )
        }
    }
}

@Composable
private fun DrivingScorePanel(
    habit: DrivingHabitProfile,
    onResetHabit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = habit.style.accentColor()
    Box(modifier = modifier.hyCard(corner = 24.dp)) {
        // 주행 습관 스타일에 따른 PLEOS 그라데이션 글로우(안정=그린/보통=블루/거침=오렌지).
        PleosGlow(
            color = habit.style.glowColor(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "주행 습관 점수",
                    color = HyTextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                // 주행습관 기록 초기화 버튼.
                TextButton(onClick = onResetHabit) {
                    Text(text = "기록 초기화", color = HyTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (habit.hasData) habit.avgScore.toString() else "--",
                    color = accent,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "점",
                    color = accent,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 6.dp, bottom = 10.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = habit.style.label(),
                        color = accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.summaryLine(),
                        color = HyTextSecondary,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            // 주행 습관 상태를 알리는 플로팅 배너(샘플 안전운전 앱의 경고 토스트 룩).
            PleosBanner(
                title = habit.style.bannerTitle(),
                description = habit.style.bannerDesc(),
                tone = habit.style.bannerTone(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HabitCell(label = "급가속", count = habit.harshAccelCount, modifier = Modifier.weight(1f))
                HabitCell(label = "급정거", count = habit.harshBrakeCount, modifier = Modifier.weight(1f))
                HabitCell(label = "부주의", count = habit.incautiousCount, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HabitCell(label: String, count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(HyTankRest, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = label, color = HyTextMuted, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = count.toString(),
                color = if (count > 0) HyWarn else HyTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "회",
                color = HyTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
            )
        }
    }
}

private fun DrivingStyle.label(): String = when (this) {
    DrivingStyle.CALM -> "안정적인 주행 습관"
    DrivingStyle.MODERATE -> "보통 주행 습관"
    DrivingStyle.AGGRESSIVE -> "거친 주행 습관"
    DrivingStyle.UNKNOWN -> "주행 습관 분석 중"
}

private fun DrivingStyle.accentColor(): Color = when (this) {
    DrivingStyle.CALM -> HyPositive
    DrivingStyle.MODERATE -> HyBlue
    DrivingStyle.AGGRESSIVE -> HyWarn
    DrivingStyle.UNKNOWN -> HyTextSecondary
}

// 점수 패널 뒤에 깔리는 PLEOS 그라데이션 글로우 색(파스텔 blur 토큰).
private fun DrivingStyle.glowColor(): Color = when (this) {
    DrivingStyle.CALM -> HyPositiveSoft
    DrivingStyle.MODERATE -> HyBlueSoft
    DrivingStyle.AGGRESSIVE -> HyWarnSoft
    DrivingStyle.UNKNOWN -> HyBlueSoft
}

// 플로팅 배너 톤/문구.
private fun DrivingStyle.bannerTone(): PleosBannerTone = when (this) {
    DrivingStyle.CALM -> PleosBannerTone.Positive
    DrivingStyle.AGGRESSIVE -> PleosBannerTone.Warning
    else -> PleosBannerTone.Info
}

private fun DrivingStyle.bannerTitle(): String = when (this) {
    DrivingStyle.CALM -> "안전 운전 중"
    DrivingStyle.MODERATE -> "양호한 주행"
    DrivingStyle.AGGRESSIVE -> "거친 주행 감지"
    DrivingStyle.UNKNOWN -> "주행 습관 분석 중"
}

private fun DrivingStyle.bannerDesc(): String = when (this) {
    DrivingStyle.CALM -> "최근 주행이 안정적이에요. 좋은 습관이 이어지고 있어요."
    DrivingStyle.MODERATE -> "조금만 더 부드럽게 운전하면 점수가 올라가요."
    DrivingStyle.AGGRESSIVE -> "급가속·급정거가 잦아요. 여유 있게 운전해보세요."
    DrivingStyle.UNKNOWN -> "주행 데이터를 모으는 중이에요."
}

/** 누적 세션/주행 시간 요약 한 줄. */
private fun DrivingHabitProfile.summaryLine(): String {
    if (!hasData) return "주행 기록을 모으는 중이에요"
    val time = when {
        totalDrivingMinutes >= 60 -> "${totalDrivingMinutes / 60}시간 ${totalDrivingMinutes % 60}분"
        else -> "${totalDrivingMinutes}분"
    }
    return "최근 ${totalSessions}회 · 누적 $time 주행 분석"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 980, heightDp = 760)
@Composable
private fun DrivingHabitDashboardPreview() {
    com.hyconnect.pleos.ui.theme.HyConnectTheme {
        DrivingHabitDashboard(
            habit = com.hyconnect.pleos.data.repository.DummyHyConnectData.drivingHabit,
            dashboard = com.hyconnect.pleos.data.repository.DummyHyConnectData.sufficientDashboard,
            onNavigate = {},
            onResetHabit = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 980, heightDp = 760)
@Composable
private fun DrivingHabitDashboardEmptyPreview() {
    com.hyconnect.pleos.ui.theme.HyConnectTheme {
        DrivingHabitDashboard(
            habit = DrivingHabitProfile(),
            dashboard = null,
            onNavigate = {},
            onResetHabit = {},
            modifier = Modifier.padding(20.dp),
        )
    }
}
