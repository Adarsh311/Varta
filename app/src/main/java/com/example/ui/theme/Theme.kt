package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VartaSaffron,
    onPrimary = VartaCharcoalDark,
    primaryContainer = VartaCardElevatedDark,
    onPrimaryContainer = VartaSaffronLight,
    secondary = VartaIvoryMuted,
    onSecondary = VartaCharcoalDark,
    secondaryContainer = VartaCardDark,
    onSecondaryContainer = VartaIvory,
    tertiary = VartaSaffronLight,
    background = VartaCharcoalDark,
    onBackground = VartaIvory,
    surface = VartaSurfaceDark,
    onSurface = VartaIvory,
    surfaceVariant = VartaCardDark,
    onSurfaceVariant = VartaIvoryMuted,
    outline = VartaHairlineDark,
    outlineVariant = VartaHairlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = VartaSaffronDeep,
    onPrimary = VartaCreamLight,
    primaryContainer = VartaCardLight,
    onPrimaryContainer = VartaSaffronDeep,
    secondary = VartaInkMuted,
    onSecondary = VartaCreamLight,
    secondaryContainer = VartaCardLight,
    onSecondaryContainer = VartaInkDark,
    tertiary = VartaSaffronDeep,
    background = VartaCreamLight,
    onBackground = VartaInkDark,
    surface = VartaSurfaceLight,
    onSurface = VartaInkDark,
    surfaceVariant = VartaCardLight,
    onSurfaceVariant = VartaInkMuted,
    outline = VartaHairlineLight,
    outlineVariant = VartaHairlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
