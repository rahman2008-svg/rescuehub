package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRedLight,
    secondary = PurpleBadgeBg,
    tertiary = BlueBadgeBg,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF25232A),
    onPrimary = OnPrimaryRedLight,
    onSecondary = OnPurpleBadge,
    onTertiary = OnBlueBadge,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    secondary = PurpleBadgeBg,
    tertiary = BlueBadgeBg,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color.White,
    onPrimary = Color.White,
    onSecondary = OnPurpleBadge,
    onTertiary = OnBlueBadge,
    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = SubtitleColor,
    outline = BorderColor
)

@Composable
fun RescueHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
