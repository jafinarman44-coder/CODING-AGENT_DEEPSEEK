// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LifeEasyColors = darkColorScheme(
    primary = Color(0xFF9B5CFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF30205C),
    onPrimaryContainer = Color(0xFFE9DEFF),
    secondary = Color(0xFF55E7FF),
    onSecondary = Color(0xFF081326),
    secondaryContainer = Color(0xFF171D3B),
    onSecondaryContainer = Color(0xFFE9DEFF),
    background = Color(0xFF050713),
    onBackground = Color(0xFFF4F3FF),
    surface = Color(0xFF050713),
    onSurface = Color(0xFFF4F3FF),
    surfaceVariant = Color(0xFF111A34),
    onSurfaceVariant = Color(0xFFA9B4D2),
    outline = Color(0xFF34446C),
    error = Color(0xFF55E7FF),
    onError = Color(0xFF081326)
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
