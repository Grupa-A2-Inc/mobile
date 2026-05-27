package com.adaptive_tutor_mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    tertiary = Tertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.65f),
    error = ErrorColor,

    // ── M3 surface-container tokens — override M3 baseline grey defaults ────
    // These are used by NavigationBar, ElevatedCard, BottomSheet, Dialog, etc.
    surfaceContainerLowest  = SurfaceLight,               // #FFFFFF
    surfaceContainerLow     = Color(0xFFF8FBFF),          // near-white
    surfaceContainer        = Color(0xFFF0F6FD),          // app background (nav bar)
    surfaceContainerHigh    = Color(0xFFE8F2FB),          // slightly more visible
    surfaceContainerHighest = Color(0xFFE0EEF9),          // text-field fill

    surfaceBright = SurfaceLight,                         // #FFFFFF
    surfaceDim    = BackgroundLight,                      // #F0F6FD

    // Inverse + outline
    inverseSurface    = Color(0xFF1A3A5C),
    inverseOnSurface  = Color(0xFFE8F2FB),
    inversePrimary    = Color(0xFF7BB8E8),
    outline           = Color(0xFFA0BAD0),
    outlineVariant    = Color(0xFFD0E4F2),
    scrim             = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    tertiary = Tertiary,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.65f),
    error = ErrorColor,

    // ── M3 surface-container tokens — override M3 baseline grey defaults ────
    surfaceContainerLowest  = BackgroundDark,             // #0D1B2A
    surfaceContainerLow     = Color(0xFF152336),          // = SurfaceDark
    surfaceContainer        = Color(0xFF1A2C40),          // nav bar
    surfaceContainerHigh    = Color(0xFF1E3048),          // = SurfaceVariantDark
    surfaceContainerHighest = Color(0xFF243859),          // text-field fill

    surfaceBright = Color(0xFF2D4560),
    surfaceDim    = BackgroundDark,

    // Inverse + outline
    inverseSurface    = Color(0xFFD6E8F7),
    inverseOnSurface  = Color(0xFF152336),
    inversePrimary    = Primary,
    outline           = Color(0xFF4A6A85),
    outlineVariant    = Color(0xFF263D57),
    scrim             = Color(0xFF000000),
)

@Composable
fun AdaptiveTutorTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward-compat alias so existing call sites still compile
@Composable
fun AdaptiveTutorMobileTheme(
    content: @Composable () -> Unit
) = AdaptiveTutorTheme(content = content)
