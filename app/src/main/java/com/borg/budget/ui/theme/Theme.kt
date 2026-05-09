package com.borg.budget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary               = SkyPrimary,
    onPrimary             = Color.White,
    primaryContainer      = Color(0xFFBBDEFB),
    onPrimaryContainer    = Color(0xFF001D36),
    secondary             = SkySecondary,
    onSecondary           = Color.White,
    secondaryContainer    = Color(0xFFE8EAF6),
    onSecondaryContainer  = Color(0xFF1A237E),
    tertiary              = SkyTertiary,
    onTertiary            = Color.White,
    tertiaryContainer     = Color(0xFFEDE7F6),
    onTertiaryContainer   = Color(0xFF1A0050),
    background            = SkyBackground,
    onBackground          = Color(0xFF1A1C2E),
    surface               = SkySurface,
    onSurface             = Color(0xFF1A1C2E),
    surfaceVariant        = Color(0xFFEEF2FF),
    onSurfaceVariant      = Color(0xFF44464F),
    outline               = SkyOutline,
    error                 = Color(0xFFE53935),
    onError               = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary               = SkyPrimaryLight,
    onPrimary             = Color(0xFF003258),
    primaryContainer      = Color(0xFF004881),
    onPrimaryContainer    = Color(0xFFD1E4FF),
    secondary             = Color(0xFF9FA8DA),
    onSecondary           = Color(0xFF1A237E),
    secondaryContainer    = Color(0xFF303F9F),
    onSecondaryContainer  = Color(0xFFE8EAF6),
    tertiary              = Color(0xFFCE93D8),
    onTertiary            = Color(0xFF1A0050),
    background            = DarkBackground,
    onBackground          = Color(0xFFE8EAF6),
    surface               = DarkSurface,
    onSurface             = Color(0xFFE8EAF6),
    surfaceVariant        = DarkSurfaceVar,
    onSurfaceVariant      = Color(0xFFCAC4D0),
    outline               = Color(0xFF605D71),
    error                 = Color(0xFFEF5350),
    onError               = Color.White,
)

@Composable
fun Quick_BudgTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
