package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VartaSaffronLight,
    onPrimary = VartaPaperDark,
    primaryContainer = VartaCardElevatedDark,
    onPrimaryContainer = VartaSaffronLight,
    secondary = VartaInkLightMuted,
    onSecondary = VartaPaperDark,
    secondaryContainer = VartaPaperCardDark,
    onSecondaryContainer = VartaInkLight,
    tertiary = VartaSaffronLight,
    background = VartaPaperDark,
    onBackground = VartaInkLight,
    surface = VartaPaperSurfaceDark,
    onSurface = VartaInkLight,
    surfaceVariant = VartaPaperCardDark,
    onSurfaceVariant = VartaInkLightMuted,
    outline = VartaHairlineDark,
    outlineVariant = VartaHairlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = VartaSaffron,
    onPrimary = VartaPaperLight,
    primaryContainer = VartaPaperCardLight,
    onPrimaryContainer = VartaInkDark,
    secondary = VartaInkMuted,
    onSecondary = VartaPaperLight,
    secondaryContainer = VartaPaperCardLight,
    onSecondaryContainer = VartaInkDark,
    tertiary = VartaPressRed,
    background = VartaPaperLight,
    onBackground = VartaInkDark,
    surface = VartaPaperSurfaceLight,
    onSurface = VartaInkDark,
    surfaceVariant = VartaPaperCardLight,
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

