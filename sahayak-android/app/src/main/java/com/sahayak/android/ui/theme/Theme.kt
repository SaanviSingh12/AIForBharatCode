package com.sahayak.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary             = SahayakPrimary,
    onPrimary           = SahayakOnPrimary,
    primaryContainer    = SahayakPrimaryContainer,
    onPrimaryContainer  = SahayakOnPrimaryContainer,
    secondary           = SahayakSecondary,
    onSecondary         = SahayakOnSecondary,
    secondaryContainer  = SahayakSecondaryContainer,
    onSecondaryContainer = SahayakOnSecondaryContainer,
    tertiary            = SahayakTertiary,
    onTertiary          = SahayakOnTertiary,
    tertiaryContainer   = SahayakTertiaryContainer,
    onTertiaryContainer = SahayakOnTertiaryContainer,
    background          = SahayakBackground,
    onBackground        = SahayakOnBackground,
    surface             = SahayakSurface,
    onSurface           = SahayakOnSurface,
    surfaceVariant      = SahayakSurfaceVariant,
    onSurfaceVariant    = SahayakOnSurfaceVariant,
    outline             = SahayakOutline,
    error               = SahayakError,
    onError             = SahayakOnError,
    errorContainer      = SahayakErrorContainer,
    onErrorContainer    = SahayakOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary             = SahayakPrimaryDark,
    onPrimary           = SahayakOnPrimaryDark,
    background          = SahayakBackgroundDark,
    onBackground        = SahayakOnBackgroundDark,
    surface             = SahayakSurfaceDark,
    onSurface           = SahayakOnSurfaceDark,
    error               = SahayakError,
    onError             = SahayakOnError,
)

@Composable
fun SahayakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
