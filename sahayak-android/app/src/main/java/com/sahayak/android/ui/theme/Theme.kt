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

// ══════════════════════════════════════════════════════════════
// Material 3 Expressive — Sahayak Theme
// ══════════════════════════════════════════════════════════════

private val LightColors = lightColorScheme(
    primary                = SahayakPrimary,
    onPrimary              = SahayakOnPrimary,
    primaryContainer       = SahayakPrimaryContainer,
    onPrimaryContainer     = SahayakOnPrimaryContainer,
    secondary              = SahayakSecondary,
    onSecondary            = SahayakOnSecondary,
    secondaryContainer     = SahayakSecondaryContainer,
    onSecondaryContainer   = SahayakOnSecondaryContainer,
    tertiary               = SahayakTertiary,
    onTertiary             = SahayakOnTertiary,
    tertiaryContainer      = SahayakTertiaryContainer,
    onTertiaryContainer    = SahayakOnTertiaryContainer,
    background             = SahayakBackground,
    onBackground           = SahayakOnBackground,
    surface                = SahayakSurface,
    onSurface              = SahayakOnSurface,
    surfaceVariant         = SahayakSurfaceVariant,
    onSurfaceVariant       = SahayakOnSurfaceVariant,
    surfaceDim             = SahayakSurfaceDim,
    surfaceBright          = SahayakSurfaceBright,
    surfaceContainerLowest = SahayakSurfaceContainerLowest,
    surfaceContainerLow    = SahayakSurfaceContainerLow,
    surfaceContainer       = SahayakSurfaceContainer,
    surfaceContainerHigh   = SahayakSurfaceContainerHigh,
    surfaceContainerHighest = SahayakSurfaceContainerHighest,
    inverseSurface         = SahayakInverseSurface,
    inverseOnSurface       = SahayakInverseOnSurface,
    inversePrimary         = SahayakInversePrimary,
    outline                = SahayakOutline,
    outlineVariant         = SahayakOutlineVariant,
    error                  = SahayakError,
    onError                = SahayakOnError,
    errorContainer         = SahayakErrorContainer,
    onErrorContainer       = SahayakOnErrorContainer,
)

private val DarkColors = darkColorScheme(
    primary                = SahayakPrimaryDark,
    onPrimary              = SahayakOnPrimaryDark,
    primaryContainer       = SahayakPrimaryContainerDark,
    onPrimaryContainer     = SahayakOnPrimaryContainerDark,
    secondary              = SahayakSecondaryDark,
    onSecondary            = SahayakOnSecondaryDark,
    secondaryContainer     = SahayakSecondaryContainerDark,
    onSecondaryContainer   = SahayakOnSecondaryContainerDark,
    tertiary               = SahayakTertiaryDark,
    onTertiary             = SahayakOnTertiaryDark,
    tertiaryContainer      = SahayakTertiaryContainerDark,
    onTertiaryContainer    = SahayakOnTertiaryContainerDark,
    background             = SahayakBackgroundDark,
    onBackground           = SahayakOnBackgroundDark,
    surface                = SahayakSurfaceDark,
    onSurface              = SahayakOnSurfaceDark,
    surfaceVariant         = SahayakSurfaceVariantDark,
    onSurfaceVariant       = SahayakOnSurfaceVariantDark,
    surfaceDim             = SahayakSurfaceDimDark,
    surfaceBright          = SahayakSurfaceBrightDark,
    surfaceContainerLowest = SahayakSurfaceContainerLowestDark,
    surfaceContainerLow    = SahayakSurfaceContainerLowDark,
    surfaceContainer       = SahayakSurfaceContainerDark,
    surfaceContainerHigh   = SahayakSurfaceContainerHighDark,
    surfaceContainerHighest = SahayakSurfaceContainerHighestDark,
    inverseSurface         = SahayakInverseSurfaceDark,
    inverseOnSurface       = SahayakInverseOnSurfaceDark,
    inversePrimary         = SahayakInversePrimaryDark,
    outline                = SahayakOutlineDark,
    outlineVariant         = SahayakOutlineVariantDark,
    error                  = SahayakError,
    onError                = SahayakOnError,
    errorContainer         = SahayakErrorContainer,
    onErrorContainer       = SahayakOnErrorContainer,
)

@Composable
fun SahayakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // M3E: prefer the custom expressive palette over dynamic color
    dynamicColor: Boolean = false,
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
        shapes = SahayakShapes,
        typography = SahayakTypography,
        content = content,
    )
}
