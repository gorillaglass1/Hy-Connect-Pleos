package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTankRest
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary

@Composable
fun HydrogenTankCard(
    vehicleState: VehicleState,
    modifier: Modifier = Modifier,
) {
    val levelColor = hydrogenLevelColor(vehicleState.hydrogenPercent)
    val gradient = hydrogenGradient(vehicleState.hydrogenPercent)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        // 라벨 행: 수소 잔량 + % + 주행거리
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "수소 잔량",
                color = HyTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "  ${vehicleState.hydrogenPercent}%",
                color = levelColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${vehicleState.vehicleRangeKm}km 주행가능",
                color = HyTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 심플 한 줄 게이지
        GaugeBar(
            percent = vehicleState.hydrogenPercent,
            gradient = gradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = vehicleState.message,
            color = HyTextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun GaugeBar(
    percent: Int,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val radius = size.height / 2f
        val fillFraction = percent.coerceIn(0, 100) / 100f

        // 트랙(빈 부분)
        drawRoundRect(
            color = HyTankRest,
            cornerRadius = CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = HyBorder,
            cornerRadius = CornerRadius(radius, radius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
        )

        // 채워진 부분
        if (fillFraction > 0f) {
            clipRect(left = 0f, top = 0f, right = size.width * fillFraction, bottom = size.height) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(gradient),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(radius, radius),
                )
            }
        }

        // 현재 위치 마커(작은 흰 원)
        val markerX = (size.width * fillFraction).coerceAtLeast(radius)
        drawCircle(
            color = Color.White,
            radius = radius - 1.dp.toPx(),
            center = Offset(markerX, size.height / 2f),
        )
    }
}

private fun hydrogenLevelColor(percent: Int): Color =
    when {
        percent < 25 -> Color(0xFFE03131)
        percent < 50 -> Color(0xFFF08C00)
        else -> HyBlue
    }

private fun hydrogenGradient(percent: Int): List<Color> =
    when {
        percent < 25 -> listOf(Color(0xFFFF8787), Color(0xFFE03131))
        percent < 50 -> listOf(Color(0xFFFFC078), Color(0xFFF08C00))
        else -> listOf(Color(0xFF53B7FF), HyBlue)
    }

@Preview(showBackground = true, widthDp = 720)
@Composable
private fun HydrogenTankCardLowPreview() {
    HyConnectTheme {
        HydrogenTankCard(
            vehicleState = VehicleState(
                hydrogenPercent = 23,
                vehicleRangeKm = 96,
                message = "충전이 필요합니다.",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 720)
@Composable
private fun HydrogenTankCardFullPreview() {
    HyConnectTheme {
        HydrogenTankCard(
            vehicleState = VehicleState(
                hydrogenPercent = 78,
                vehicleRangeKm = 500,
                message = "수소 충전량이 충분합니다.",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
