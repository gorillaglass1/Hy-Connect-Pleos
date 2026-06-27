package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hyconnect.pleos.data.model.HydrogenStation
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyBlueSoft
import com.hyconnect.pleos.ui.theme.HySurface
import com.hyconnect.pleos.ui.theme.HyTextPrimary
import com.hyconnect.pleos.ui.theme.HyTextSecondary
import com.hyconnect.pleos.ui.theme.hyCard

/**
 * 경유지로 추가하기 전에 사용자 확인을 받는 팝업.
 * "추가" 시 onConfirm으로 경위도 기반 경유지 추가(addWaypoint)를 호출한다.
 */
@Composable
fun WaypointConfirmDialog(
    station: HydrogenStation,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hyCard(corner = 28.dp)
                .padding(28.dp),
        ) {
            Text(
                text = "경유지 추가",
                color = HyBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "현재 경로에 이 충전소를\n경유지로 추가할까요?",
                color = HyTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HyBlueSoft, RoundedCornerShape(16.dp))
                    .padding(18.dp),
            ) {
                Text(
                    text = station.name,
                    color = HyTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = station.address,
                    color = HyTextSecondary,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "%.1fkm".format(station.distanceKm),
                        color = HyBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "대기 ${station.waitMinutes}분",
                        color = HyTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = station.pressureInfo,
                        color = HyTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Text(
                        text = "취소",
                        color = HyTextSecondary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HyBlue,
                        contentColor = HySurface,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Text(
                        text = "경유지 추가",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
