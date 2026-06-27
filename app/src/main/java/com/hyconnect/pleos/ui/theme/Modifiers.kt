package com.hyconnect.pleos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hyconnect.pleos.ui.dropShadow

// PLEOS 샘플 카드 룩: 흰 면 + 중성 보더 + 부드러운 드롭섀도우 + 24dp 라운드.
fun Modifier.hyCard(
    corner: Dp = 24.dp,
    surface: Color = HySurface,
    border: Color = HyBorderStrong,
): Modifier {
    val shape: Shape = if (corner > 0.dp) RoundedCornerShape(corner) else RectangleShape
    return this
        .dropShadow(
            shape = shape,
            color = Color(0x14131417),
            blur = 24.dp,
            offsetY = 8.dp,
        )
        .clip(shape)
        .background(surface, shape)
        .border(1.dp, border, shape)
}

// PLEOS 글로우: 샘플 앱 BackgroundEffect(거대한 블러 드롭섀도우)를 차용해
// 면 뒤로 은은한 컬러 그라데이션을 피운다. 상태별 파스텔 blur 토큰(블루/그린/오렌지)을
// 색으로 넘기면 하단에서 올라오는 그라데이션 틴트가 만들어진다.
fun Modifier.pleosGlow(
    color: Color,
    blur: Dp = 160.dp,
    offsetY: Dp = 80.dp,
): Modifier = this.dropShadow(
    shape = RoundedCornerShape(percent = 100),
    color = color,
    blur = blur,
    offsetY = offsetY,
)
