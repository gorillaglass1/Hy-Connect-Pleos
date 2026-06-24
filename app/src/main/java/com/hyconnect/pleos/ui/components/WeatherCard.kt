package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.network.WeatherResponse
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextMuted
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.HyWarn

/**
 * 연료 충분 화면의 날씨 카드. 아이콘은 sky/precipitation_type에 따라 분기한다.
 */
@Composable
fun WeatherCard(
    weather: WeatherResponse?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(24.dp),
    ) {
        when {
            error != null -> WeatherMessage("날씨를 불러오지 못했어요", error, HyWarn)
            weather == null -> WeatherMessage(
                if (isLoading) "날씨를 불러오는 중…" else "날씨 정보가 없어요", null, HyTextPrimary,
            )
            else -> WeatherContent(weather)
        }
    }
}

@Composable
private fun WeatherContent(weather: WeatherResponse) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(HyBlueSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = weatherIcon(weather.sky, weather.precipitationType),
                    contentDescription = weather.sky,
                    tint = HyBlue,
                    modifier = Modifier.size(38.dp),
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = "${weather.temperature.toInt()}°",
                    color = HyTextPrimary,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = weather.sky,
                    color = HyTextSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            WeatherStat("체감", "${weather.feelsLike.toInt()}°", Modifier.weight(1f))
            WeatherStat("습도", "${weather.humidity.toInt()}%", Modifier.weight(1f))
            WeatherStat("바람", "${weather.windSpeed.trimNumber()}m/s", Modifier.weight(1f))
        }
    }
}

@Composable
private fun WeatherStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = HyTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = HyTextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun WeatherMessage(title: String, detail: String?, titleColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, color = titleColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (detail != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = detail, color = HyTextSecondary, fontSize = 14.sp)
        }
    }
}

private fun weatherIcon(sky: String, precipitationType: String): ImageVector =
    when (precipitationType) {
        "없음" -> when (sky) {
            "맑음" -> Icons.Default.WbSunny
            "구름많음" -> Icons.Default.WbCloudy
            "흐림" -> Icons.Default.Cloud
            else -> Icons.Default.Cloud // 알수없음/그 외
        }
        "비", "소나기" -> Icons.Default.Umbrella
        "눈", "비/눈" -> Icons.Default.AcUnit
        else -> Icons.Default.Cloud // 예기치 못한 강수값 폴백
    }

private fun Double.trimNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

@Preview(showBackground = true, widthDp = 720, heightDp = 320)
@Composable
private fun WeatherCardPreview() {
    HyConnectTheme {
        WeatherCard(
            weather = WeatherResponse(
                temperature = 21.0, sky = "구름많음", precipitationType = "없음",
                humidity = 85.0, windSpeed = 1.0, feelsLike = 21.0,
                baseTime = "1400", nx = 60, ny = 127,
            ),
            isLoading = false,
            error = null,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 320)
@Composable
private fun WeatherCardRainPreview() {
    HyConnectTheme {
        WeatherCard(
            weather = WeatherResponse(
                temperature = 18.0, sky = "흐림", precipitationType = "비",
                humidity = 90.0, windSpeed = 2.5, feelsLike = 17.0,
                baseTime = "1500", nx = 60, ny = 127,
            ),
            isLoading = false,
            error = null,
            modifier = Modifier.padding(24.dp),
        )
    }
}
