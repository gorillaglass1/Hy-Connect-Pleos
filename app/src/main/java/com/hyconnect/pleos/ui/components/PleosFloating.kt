package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.ui.dropShadow
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorderStrong
import com.hyconnect.pleos.ui.theme.HyPositive
import com.hyconnect.pleos.ui.theme.HyPositiveSoft
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.HyWarn
import com.hyconnect.pleos.ui.theme.HyWarnSoft
import com.hyconnect.pleos.ui.theme.pleosGlow

/** PLEOS 배너 톤: 인포(블루)/포지티브(그린)/경고(레드). */
enum class PleosBannerTone { Info, Positive, Warning }

/**
 * 화면/카드 하단에서 은은하게 피어오르는 PLEOS 그라데이션 글로우.
 * 샘플 앱의 BackgroundEffect를 차용했다. 보통 Box의 BottomCenter에 배치하고
 * fillMaxWidth + 고정 height를 준 뒤 컨텐츠 뒤(가장 아래 z)에 깐다.
 */
@Composable
fun PleosGlow(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.pleosGlow(color))
}

/**
 * PLEOS 플로팅 배너(샘플 앱의 WarningToast 룩).
 * 흰 면 + 라운드 20 + 부드러운 섀도 + 톤 컬러 보더 + 아이콘 칩.
 */
@Composable
fun PleosBanner(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    tone: PleosBannerTone = PleosBannerTone.Info,
    glyph: String = tone.glyph(),
) {
    val shape = RoundedCornerShape(20.dp)
    val accent = tone.accent()
    val isWarning = tone == PleosBannerTone.Warning
    Row(
        modifier = modifier
            .dropShadow(
                shape = shape,
                color = Color(0x14131417),
                blur = 20.dp,
                offsetY = 8.dp,
            )
            .clip(shape)
            .background(HySurface, shape)
            .border(
                width = if (isWarning) 1.5.dp else 1.dp,
                color = if (isWarning) accent.copy(alpha = 0.55f) else HyBorderStrong,
                shape = shape,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tone.tint()),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = glyph,
                color = accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = HyTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun PleosBannerTone.accent(): Color = when (this) {
    PleosBannerTone.Info -> HyBlue
    PleosBannerTone.Positive -> HyPositive
    PleosBannerTone.Warning -> HyWarn
}

private fun PleosBannerTone.tint(): Color = when (this) {
    PleosBannerTone.Info -> HyBlueSoft
    PleosBannerTone.Positive -> HyPositiveSoft
    PleosBannerTone.Warning -> HyWarnSoft
}

private fun PleosBannerTone.glyph(): String = when (this) {
    PleosBannerTone.Info -> "i"
    PleosBannerTone.Positive -> "✓"
    PleosBannerTone.Warning -> "!"
}
