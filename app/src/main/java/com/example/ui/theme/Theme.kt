package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeadzoneDarkScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color(0xFF1A0A02),
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = Color(0xFFFFDCCB),
    secondary = CyberCyan,
    onSecondary = Color(0xFF002029),
    secondaryContainer = CyberCyanContainer,
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = ScrapGold,
    onTertiary = Color(0xFF231900),
    tertiaryContainer = ScrapGoldContainer,
    onTertiaryContainer = Color(0xFFFEF08A),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = TacticalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TacticalBorder,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun DeadzoneDriveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeadzoneDarkScheme,
        typography = AppTypography,
        content = content
    )
}
