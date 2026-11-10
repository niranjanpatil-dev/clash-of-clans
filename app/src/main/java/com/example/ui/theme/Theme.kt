package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProfessionalColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,

    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = TextPrimary,

    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldSuccess,

    error = RedDanger,
    onError = Color.White,
    errorContainer = RedDangerContainer,
    onErrorContainer = RedDanger,

    background = LightBackground,
    onBackground = TextPrimary,

    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightCardBackground,
    onSurfaceVariant = TextSecondary,

    outline = LightBorder,
    outlineVariant = LightBorderVariant
)

@Composable
fun ClashTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProfessionalColorScheme,
        typography = Typography,
        content = content
    )
}
