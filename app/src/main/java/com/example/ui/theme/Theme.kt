package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MareaDeepBlue,
    secondary = MareaBlue,
    tertiary = MareaTurquoise,
    background = MareaBackground,
    surface = MareaSurface,
    onPrimary = MareaTextLight,
    onSecondary = MareaTextLight,
    onTertiary = MareaTextLight,
    onBackground = MareaTextDark,
    onSurface = MareaTextDark,
    surfaceVariant = MareaCardLight,
    onSurfaceVariant = MareaTextDark
)

@Composable
fun MareaClubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MareaTypography,
        content = content
    )
}
