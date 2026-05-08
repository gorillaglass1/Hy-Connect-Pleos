package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.VehicleState
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
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
    val tankColors = hydrogenLevelColors(vehicleState.hydrogenPercent)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "수소 잔량",
                color = HyTextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${vehicleState.hydrogenPercent}%",
                color = tankColors.primary,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 56.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = vehicleState.message,
                color = HyTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "예상 주행 가능 거리 ${vehicleState.vehicleRangeKm}km",
                color = HyTextSecondary,
                fontSize = 15.sp,
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        HydrogenTankVisual(
            percent = vehicleState.hydrogenPercent,
            colors = tankColors,
            modifier = Modifier
                .width(260.dp)
                .height(104.dp),
        )
    }
}

@Composable
private fun HydrogenTankVisual(
    percent: Int,
    colors: HydrogenLevelColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(colors.container, RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(54.dp)) {
            val capWidth = 18.dp.toPx()
            val tankWidth = size.width - capWidth
            val tankHeight = size.height
            val radius = tankHeight / 2f
            val fillWidth = tankWidth * percent.coerceIn(0, 100) / 100f

            drawRoundRect(
                color = HyTankRest,
                topLeft = Offset.Zero,
                size = Size(tankWidth, tankHeight),
                cornerRadius = CornerRadius(radius, radius),
            )
            drawRoundRect(
                color = HyBorder,
                topLeft = Offset.Zero,
                size = Size(tankWidth, tankHeight),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = 2.dp.toPx()),
            )

            clipRect(left = 0f, top = 0f, right = fillWidth, bottom = tankHeight) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors.gradient,
                    ),
                    topLeft = Offset.Zero,
                    size = Size(tankWidth, tankHeight),
                    cornerRadius = CornerRadius(radius, radius),
                )
            }

            val bubbles = listOf(
                Offset(tankWidth * 0.18f, tankHeight * 0.35f) to 4.dp.toPx(),
                Offset(tankWidth * 0.31f, tankHeight * 0.64f) to 3.dp.toPx(),
                Offset(tankWidth * 0.47f, tankHeight * 0.40f) to 5.dp.toPx(),
                Offset(tankWidth * 0.58f, tankHeight * 0.68f) to 3.5.dp.toPx(),
                Offset(tankWidth * 0.70f, tankHeight * 0.34f) to 4.dp.toPx(),
            )
            bubbles.forEach { (center, radiusPx) ->
                if (center.x < fillWidth - radiusPx) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.46f),
                        radius = radiusPx,
                        center = center,
                    )
                }
            }

            drawRoundRect(
                color = Color(0xFFC7D0DA),
                topLeft = Offset(tankWidth - 1.dp.toPx(), tankHeight * 0.24f),
                size = Size(capWidth, tankHeight * 0.52f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
        }
    }
}

private data class HydrogenLevelColors(
    val primary: Color,
    val container: Color,
    val gradient: List<Color>,
)

private fun hydrogenLevelColors(percent: Int): HydrogenLevelColors =
    when {
        percent < 25 -> HydrogenLevelColors(
            primary = Color(0xFFE03131),
            container = Color(0xFFFFECEC),
            gradient = listOf(Color(0xFFFF8787), Color(0xFFE03131), Color(0xFFC92A2A)),
        )
        percent < 50 -> HydrogenLevelColors(
            primary = Color(0xFFF08C00),
            container = Color(0xFFFFF3D6),
            gradient = listOf(Color(0xFFFFC078), Color(0xFFF08C00), Color(0xFFE67700)),
        )
        else -> HydrogenLevelColors(
            primary = HyBlue,
            container = HyBlueSoft,
            gradient = listOf(Color(0xFF53B7FF), HyBlue, Color(0xFF0F5FD7)),
        )
    }

@Preview(showBackground = true, widthDp = 720)
@Composable
private fun HydrogenTankCardPreview() {
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
