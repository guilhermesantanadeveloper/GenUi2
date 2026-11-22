package com.guilherme.santana.genui2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NuPrimary,
    onPrimary = NuOnPrimary,
    primaryContainer = NuSecondary,
    onPrimaryContainer = NuOnSecondary,
    secondary = NuSecondary,
    onSecondary = NuOnSecondary,
    secondaryContainer = NuSecondary,
    onSecondaryContainer = NuOnSecondary,
    background = NuDarkBackground,
    onBackground = NuDarkOnBackground,
    surface = NuDarkSurface,
    onSurface = NuDarkOnSurface,
    surfaceVariant = NuDarkSurfaceVariant,
    onSurfaceVariant = NuDarkOnSurfaceVariant,
    outline = NuDarkOutline,
    error = NuError,
    onError = NuOnError
)

private val LightColorScheme = lightColorScheme(
    primary = NuPrimary,
    onPrimary = NuOnPrimary,
    primaryContainer = NuPrimaryContainer,
    onPrimaryContainer = NuOnPrimaryContainer,
    secondary = NuSecondary,
    onSecondary = NuOnSecondary,
    secondaryContainer = NuSecondaryContainer,
    onSecondaryContainer = NuOnSecondaryContainer,
    background = NuBackground,
    onBackground = NuOnBackground,
    surface = NuSurface,
    onSurface = NuOnSurface,
    surfaceVariant = NuSurfaceVariant,
    onSurfaceVariant = NuOnSurfaceVariant,
    outline = NuOutline,
    outlineVariant = NuOutlineVariant,
    error = NuError,
    onError = NuOnError
)

@Composable
fun GenUi2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) { }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}