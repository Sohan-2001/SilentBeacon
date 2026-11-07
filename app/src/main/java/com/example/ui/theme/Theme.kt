package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DayLightColorScheme = lightColorScheme(
    primary = EmergencyRed,
    onPrimary = Color.White,
    primaryContainer = EmergencyRedLight,
    onPrimaryContainer = EmergencyRedHover,
    secondary = ActionBlue,
    onSecondary = Color.White,
    secondaryContainer = ActionBlueLight,
    onSecondaryContainer = ActionBlue,
    tertiary = CalmGreen,
    onTertiary = Color.White,
    background = DayBackground,
    onBackground = DayTextPrimary,
    surface = DaySurface,
    onSurface = DayTextPrimary,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayTextSecondary,
    outline = DayBorder,
    error = EmergencyRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Explicit Day / Light mode as requested by user
    MaterialTheme(
        colorScheme = DayLightColorScheme,
        typography = Typography,
        content = content
    )
}
