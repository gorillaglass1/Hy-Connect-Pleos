package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.AiRecommendation
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary

@Composable
fun AiRecommendationCard(
    recommendation: AiRecommendation,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HySurface, RoundedCornerShape(20.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(20.dp))
            .padding(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recommendation.label,
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.title,
                color = HyTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = recommendation.dustSummary,
                color = HyTextSecondary,
                fontSize = 17.sp,
            )
            Text(
                text = recommendation.routeSummary,
                color = HyTextSecondary,
                fontSize = 17.sp,
            )
        }

        Column(
            modifier = Modifier
                .size(width = 92.dp, height = 104.dp)
                .background(HyBlueSoft, RoundedCornerShape(18.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Canvas(modifier = Modifier.size(width = 42.dp, height = 38.dp)) {
                val barWidth = 8.dp.toPx()
                val gap = 6.dp.toPx()
                val heights = listOf(15.dp.toPx(), 26.dp.toPx(), 36.dp.toPx())
                heights.forEachIndexed { index, height ->
                    val left = index * (barWidth + gap)
                    drawRoundRect(
                        color = HyBlue,
                        topLeft = Offset(left, size.height - height),
                        size = Size(barWidth, height),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "활용",
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 720)
@Composable
private fun AiRecommendationCardPreview() {
    HyConnectTheme {
        AiRecommendationCard(
            recommendation = AiRecommendation(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
