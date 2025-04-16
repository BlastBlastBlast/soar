package no.uio.ifi.in2000.met2025.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.met2025.R

// Dark mode
private val DarkColorScheme = darkColorScheme(
    primary = WarmOrange,      // Accent color remains the warm orange.
    onPrimary = Black,         // Text on a warm orange background is black.
    secondary = White,         // Use white for secondary elements.
    onSecondary = Black,
    background = Black,        // Dark background.
    onBackground = White,
    surface = Black,           // Cards and surfaces are dark.
    onSurface = White
)

// Light mode
private val LightColorScheme = lightColorScheme(
    primary = WarmOrange,      // Accent color remains warm orange.
    onPrimary = White,         // White text on warm orange.
    secondary = Black,         // Black used for secondary elements.
    onSecondary = White,
    background = White,        // Light background.
    onBackground = Black,
    surface = White,           // Cards and surfaces are light.
    onSurface = Black
)

@Composable
fun In2000_met2025_team21Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Use dynamic color if available.
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        // If dynamic colors are available on Android S+ devices, override specific attributes.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    primary = WarmOrange,
                    onPrimary = Black,
                    background = Black,
                    onBackground = White,
                    surface = Black,
                    onSurface = White
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    primary = WarmOrange,
                    onPrimary = White,
                    background = White,
                    onBackground = Black,
                    surface = White,
                    onSurface = Black
                )
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
