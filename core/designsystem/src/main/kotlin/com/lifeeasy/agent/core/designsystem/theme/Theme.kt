// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LifeEasyBlue,
    onPrimary = Color.White,
    primaryContainer = LifeEasyBlueLight,
    onPrimaryContainer = LifeEasyNavy,
    secondary = LifeEasyTeal,
    onSecondary = Color.White,
    secondaryContainer = LifeEasyMint,
    onSecondaryContainer = LifeEasyNavy,
    background = LifeEasyBackground,
    onBackground = LifeEasyText,
    surface = LifeEasySurface,
    onSurface = LifeEasyText,
    surfaceVariant = LifeEasyBlueLight,
    onSurfaceVariant = LifeEasyMuted,
    outline = LifeEasyBorder,
    error = LifeEasyError
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CC8FF),
    onPrimary = Color(0xFF003354),
    secondary = Color(0xFF77D5D2),
    background = LifeEasyDarkBackground,
    onBackground = LifeEasyDarkText,
    surface = LifeEasyDarkSurface,
    onSurface = LifeEasyDarkText,
    onSurfaceVariant = LifeEasyDarkMuted,
    error = Color(0xFFFFB4AB)
)

@Composable
fun LifeEasyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LifeEasyTypography,
        content = content
    )
}
