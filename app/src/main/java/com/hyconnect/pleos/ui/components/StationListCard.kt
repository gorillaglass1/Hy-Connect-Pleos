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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBorder
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary

@Composable
fun StationListCard(
    stations: List<HydrogenStation>,
    isLoading: Boolean,
    errorMessage: String?,
    onRouteClick: (HydrogenStation) -> Unit,
    onMoreClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "경로 선택",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HySurface, RoundedCornerShape(22.dp))
            .border(1.dp, HyBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "추천 충전소",
                    color = HyTextPrimary,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "현재 위치 기준",
                    color = HyTextSecondary,
                    fontSize = 15.sp,
                )
            }
            Text(
                text = "새로고침",
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(enabled = !isLoading, onClick = onRefreshClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading && stations.isEmpty() -> LoadingState()
                stations.isEmpty() -> EmptyState(errorMessage = errorMessage)
                else -> StationList(
                    stations = stations,
                    onRouteClick = onRouteClick,
                    actionLabel = actionLabel,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "더보기",
            color = HyBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onMoreClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun StationList(
    stations: List<HydrogenStation>,
    onRouteClick: (HydrogenStation) -> Unit,
    actionLabel: String,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(
            items = stations,
            key = { _, station -> station.id },
        ) { index, station ->
            StationRow(
                station = station,
                onRouteClick = onRouteClick,
                actionLabel = actionLabel,
            )
            if (index != stations.lastIndex) {
                HorizontalDivider(color = HyBorder)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = HyBlue)
    }
}

@Composable
private fun EmptyState(errorMessage: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = errorMessage ?: "추천 가능한 충전소가 없습니다.",
            color = HyTextSecondary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 520)
@Composable
private fun StationListCardPreview() {
    HyConnectTheme {
        StationListCard(
            stations = listOf(
                HydrogenStation(
                    id = "1",
                    name = "현대 수소충전소 양재",
                    address = "서울 서초구 바우뫼로 12길 123",
                    status = "운영 중",
                    pressureInfo = "700bar 사용 가능",
                    distanceKm = 2.1,
                    waitMinutes = 5,
                    isRecommended = true,
                ),
                HydrogenStation(
                    id = "2",
                    name = "가이아 수소충전소",
                    address = "경기 성남시 분당구 구미로 289",
                    status = "운영 중",
                    pressureInfo = "700bar 사용 가능",
                    distanceKm = 4.5,
                    waitMinutes = 10,
                ),
            ),
            isLoading = false,
            errorMessage = null,
            onRouteClick = {},
            onMoreClick = {},
            onRefreshClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
