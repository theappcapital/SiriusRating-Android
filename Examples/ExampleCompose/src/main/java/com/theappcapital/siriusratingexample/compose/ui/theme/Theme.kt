package com.theappcapital.siriusratingexample.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AccentBlueLight,
    onPrimary = Color.White,
    background = GroupedBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = CellBackgroundLight,
    onSurface = TextPrimaryLight,
    error = AccentRedLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlueDark,
    onPrimary = Color.White,
    background = GroupedBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = CellBackgroundDark,
    onSurface = TextPrimaryDark,
    error = AccentRedDark,
    onError = Color.White,
)

object AppColors {
    val success: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) AccentGreenDark else AccentGreenLight

    val danger: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) AccentRedDark else AccentRedLight
}

@Composable
fun SiriusRatingExampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
