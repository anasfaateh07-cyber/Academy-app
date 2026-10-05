package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = Color.White,
    primaryContainer = IslamicGreenContainer,
    onPrimaryContainer = IslamicOnGreenContainer,
    secondary = IslamicGold,
    onSecondary = Color(0xFF332501),
    secondaryContainer = IslamicGoldContainer,
    onSecondaryContainer = IslamicOnGoldContainer,
    tertiary = IslamicGreenLight,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimaryCharcoal,
    surface = SurfaceWhite,
    onSurface = TextPrimaryCharcoal,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondaryGrey,
    outline = BorderGold,
    outlineVariant = DividerMuted,
    error = ColorError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = IslamicGreenLight,
    onPrimary = Color.White,
    primaryContainer = IslamicGreenDark,
    onPrimaryContainer = IslamicGreenContainer,
    secondary = IslamicGoldLight,
    onSecondary = Color(0xFF3D2E00),
    secondaryContainer = IslamicGoldDark,
    onSecondaryContainer = IslamicGoldContainer,
    background = Color(0xFF0F1E13),
    onBackground = Color(0xFFF3F7F2),
    surface = Color(0xFF172B1C),
    onSurface = Color(0xFFF3F7F2),
    outline = IslamicGold,
    error = ColorError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Islamic Green & Gold branding consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
