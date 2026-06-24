package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.hyconnect.pleos.ui.theme.HyTextSecondary

@Composable
fun HydrogenTankCard(
    vehicleState: VehicleState,
    modifier: Modifier = Modifier,
) {
    val levelColor = hydrogenLevelColor(vehicleState.hydrogenPercent)
    val gradient = hydrogenGradient(vehicleState.hydrogenPercent)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HySurface, RoundedCornerShape(16.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "수소",
            color = HyTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.width(12.dp))
        GaugeBar(
            percent = vehicleState.hydrogenPercent,
            gradient = gradient,
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${vehicleState.hydrogenPercent}%",
            color = levelColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "${vehicleState.vehicleRangeKm}km",
            color = HyTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
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

        drawRoundRect(
            color = HyTankRest,
            cornerRadius = CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = HyBorder,
            cornerRadius = CornerRadius(radius, radius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
        )

        if (fillFraction > 0f) {
            clipRect(left = 0f, top = 0f, right = size.width * fillFraction, bottom = size.height) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(gradient),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(radius, radius),
                )
            }
        }

        val markerX = (size.width * fillFraction).coerceIn(radius, size.width - radius)
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
                vehicleRangeKm = 500,
                message = "수소 충전량이 충분합니다.",
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
