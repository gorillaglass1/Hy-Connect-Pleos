package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hyconnect.pleos.settings.RefreshSettings
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorderStrong
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.hyCard

/** 자동 새로고침 주기 선택 후보(초). 0은 비활성화(끔). */
private val REFRESH_OPTIONS = listOf(0, 60, 180, 300, 600)

/**
 * 화면별 자동 새로고침 주기를 설정하는 팝업.
 *
 * - 추천 화면(LOW): 연료가 적을 때 주기적으로 현재 위치 기준 충전소를 다시 추천한다(기본 300초).
 * - 메인 대시보드(SUFFICIENT): 기본 비활성화.
 *
 * 선택은 즉시 [onLowChange]/[onDashboardChange]로 저장되며, 진행 중인 새로고침 루프에 바로 반영된다.
 */
@Composable
fun RefreshSettingsDialog(
    settings: RefreshSettings,
    onLowChange: (Int) -> Unit,
    onDashboardChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hyCard(corner = 28.dp)
                .padding(28.dp),
        ) {
            Text(
                text = "자동 새로고침",
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "주기마다 현재 위치 기준으로\n화면을 다시 불러옵니다.",
                color = HyTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            RefreshOptionSection(
                title = "충전소 추천 화면",
                description = "연료가 적을 때 이동하면서도 가까운 충전소를 추천받습니다.",
                selectedSec = settings.lowRefreshSec,
                onSelect = onLowChange,
            )

            Spacer(modifier = Modifier.height(20.dp))

            RefreshOptionSection(
                title = "메인 대시보드",
                description = "운전 습관·날씨 대시보드를 주기적으로 갱신합니다.",
                selectedSec = settings.dashboardRefreshSec,
                onSelect = onDashboardChange,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HyBlue,
                    contentColor = HySurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = "완료",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RefreshOptionSection(
    title: String,
    description: String,
    selectedSec: Int,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = HyTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = HyTextSecondary,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            REFRESH_OPTIONS.forEach { sec ->
                RefreshOptionChip(
                    label = labelFor(sec),
                    selected = sec == selectedSec,
                    onClick = { onSelect(sec) },
                )
            }
        }
    }
}

@Composable
private fun RefreshOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val container = if (selected) HyBlue else HyBlueSoft
    val content = if (selected) HySurface else HyTextPrimary
    Row(
        modifier = Modifier
            .clip(shape)
            .background(container, shape)
            .then(
                if (selected) Modifier else Modifier.border(1.dp, HyBorderStrong, shape),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun labelFor(seconds: Int): String = when {
    seconds <= RefreshSettings.DISABLED -> "끔"
    seconds % 60 == 0 -> "${seconds / 60}분"
    else -> "${seconds}초"
}
