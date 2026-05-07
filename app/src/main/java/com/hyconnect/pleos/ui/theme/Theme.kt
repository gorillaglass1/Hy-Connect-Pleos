package com.hyconnect.pleos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HyColorScheme = lightColorScheme(
    primary = HyBlue,
    onPrimary = HySurface,
    primaryContainer = HyBlueSoft,
    onPrimaryContainer = HyBlueDark,
    background = HyBackground,
    onBackground = HyTextPrimary,
    surface = HySurface,
    onSurface = HyTextPrimary,
    surfaceVariant = HyBlueSoft,
    onSurfaceVariant = HyTextSecondary,
    outline = HyBorder,
)

@Composable
fun HyConnectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HyColorScheme,
        typography = HyTypography,
        content = content,
    )
}
