package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextMuted
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.HyWarn
import com.hyconnect.pleos.ui.theme.HyWarnSoft
import com.hyconnect.pleos.ui.theme.hyCard

/**
 * 연료가 임계값 이하일 때 상단에 띄우는 경고 배너.
 * 추천 서버가 내려준 운전자 메시지(driverMessage)를 함께 보여준다.
 */
@Composable
fun LowFuelBanner(
    rangeKm: Int,
    driverMessage: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HyWarnSoft, RoundedCornerShape(24.dp))
            .border(2.dp, HyWarn, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(HyWarn, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "!",
                color = HySurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "충전이 필요합니다 · 주행가능 ${rangeKm}km",
                color = HyWarn,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (!driverMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = driverMessage,
                    color = HyTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/**
 * 자연어로 충전소를 검색하는 입력 바. 입력값은 그대로 nl_query로 서버에 전달된다.
 */
@Composable
fun NlQueryBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = {
                Text(
                    text = "예) 가까운 충전소 추천해줘",
                    color = HyTextMuted,
                    fontSize = 16.sp,
                )
            },
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HyBlue,
                unfocusedBorderColor = HyBorder,
                focusedContainerColor = HySurface,
                unfocusedContainerColor = HySurface,
                focusedTextColor = HyTextPrimary,
                unfocusedTextColor = HyTextPrimary,
            ),
        )
        VoiceMicButton(onClick = onVoiceClick)
        Button(
            onClick = { onSearch(query) },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HyBlue,
                contentColor = HySurface,
            ),
            modifier = Modifier.size(width = 88.dp, height = 56.dp),
        ) {
            Text(text = "검색", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VoiceMicButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(HyBlueSoft, RoundedCornerShape(16.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 간단한 마이크 글리프(추후 Gleo AI STT 연결 시 교체).
        Text(text = "🎙", fontSize = 22.sp)
    }
}

/**
 * 연료가 충분할 때 보여줄 자리. 날씨+교통 결합 대시보드는 추후 제작 예정이라 플레이스홀더를 둔다.
 */
@Composable
fun DashboardPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .hyCard(corner = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(HyBlueSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "☀", fontSize = 34.sp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "주행 대시보드",
            color = HyTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "날씨와 교통 상황을 결합한 대시보드를 준비하고 있어요.",
            color = HyTextSecondary,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "연료가 넉넉할 때는 이 화면이 표시됩니다.",
            color = HyTextMuted,
            fontSize = 14.sp,
        )
    }
}
