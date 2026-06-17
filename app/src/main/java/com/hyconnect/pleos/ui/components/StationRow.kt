package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary

@Composable
fun StationRow(
    station: HydrogenStation,
    onRouteClick: (HydrogenStation) -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "경로 선택",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 92.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocationPin(modifier = Modifier.size(42.dp))
        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = station.name,
                    color = HyTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (station.isRecommended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "추천",
                        color = HySurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(HyBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = station.address,
                color = HyTextSecondary,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = station.status,
                    color = HyBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = station.pressureInfo,
                    color = HyTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.1fkm".format(station.distanceKm),
                color = HyTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "대기 ${station.waitMinutes}분",
                color = HyTextSecondary,
                fontSize = 15.sp,
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Button(
            onClick = { onRouteClick(station) },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HyBlue,
                contentColor = HySurface,
            ),
            modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 52.dp),
        ) {
            Text(
                text = actionLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LocationPin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(HyBlueSoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            drawCircle(
                color = HyBlue,
                radius = 8.dp.toPx(),
                center = Offset(size.width / 2f, 8.dp.toPx()),
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(size.width / 2f, 8.dp.toPx()),
            )
            drawLine(
                color = HyBlue,
                start = Offset(size.width / 2f, 15.dp.toPx()),
                end = Offset(size.width / 2f, 22.dp.toPx()),
                strokeWidth = 5.dp.toPx(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun StationRowPreview() {
    HyConnectTheme {
        StationRow(
            station = HydrogenStation(
                id = "preview",
                name = "현대 수소충전소 양재",
                address = "서울 서초구 바우뫼로 12길 123",
                status = "운영 중",
                pressureInfo = "700bar 사용 가능",
                distanceKm = 2.1,
                waitMinutes = 5,
                isRecommended = true,
            ),
            onRouteClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
