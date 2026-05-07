package com.hyconnect.pleos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.hyconnect.pleos.ui.theme.HyBlue
import com.hyconnect.pleos.ui.theme.HyConnectTheme
import com.hyconnect.pleos.ui.theme.HySurface

@Composable
fun VoiceCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HyBlue,
            contentColor = HySurface,
        ),
        modifier = modifier.defaultMinSize(minWidth = 132.dp, minHeight = 52.dp),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp),
        ) {
            MicrophoneGlyph(modifier = Modifier.size(18.dp))
            Text(text = "음성 호출", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MicrophoneGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        drawRoundRect(
            color = HySurface,
            topLeft = Offset(size.width * 0.34f, 0f),
            size = Size(size.width * 0.32f, size.height * 0.58f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.18f, size.height * 0.42f),
            end = Offset(size.width * 0.18f, size.height * 0.58f),
            strokeWidth = stroke,
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.82f, size.height * 0.42f),
            end = Offset(size.width * 0.82f, size.height * 0.58f),
            strokeWidth = stroke,
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.18f, size.height * 0.58f),
            end = Offset(size.width * 0.50f, size.height * 0.78f),
            strokeWidth = stroke,
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.82f, size.height * 0.58f),
            end = Offset(size.width * 0.50f, size.height * 0.78f),
            strokeWidth = stroke,
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.50f, size.height * 0.78f),
            end = Offset(size.width * 0.50f, size.height),
            strokeWidth = stroke,
        )
        drawLine(
            color = HySurface,
            start = Offset(size.width * 0.33f, size.height),
            end = Offset(size.width * 0.67f, size.height),
            strokeWidth = stroke,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoiceCallButtonPreview() {
    HyConnectTheme {
        VoiceCallButton(onClick = {})
    }
}
