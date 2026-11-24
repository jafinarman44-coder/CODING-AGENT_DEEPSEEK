// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LifeEasyColors = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3B1010),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFF6B6B),
    onSecondary = Color(0xFF2B0808),
    secondaryContainer = Color(0xFF2A1717),
    onSecondaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF666666),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0808)
)

/** Dark high-contrast theme is the app default; callers cannot accidentally fall back to light mode. */
@Composable
fun LifeEasyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeEasyColors,
        typography = LifeEasyTypography,
        content = content
    )
}
