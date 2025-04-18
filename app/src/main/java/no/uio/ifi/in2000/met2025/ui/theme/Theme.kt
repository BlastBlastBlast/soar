package no.uio.ifi.in2000.met2025.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalIsDarkTheme = staticCompositionLocalOf { false }
val LocalAppCursorColor = staticCompositionLocalOf { Color.Black }


// Dark mode
private val DarkColorScheme = darkColorScheme(
    primary = Color(0x2A262626),      // Accent color remains the warm orange.
    onPrimary = WarmOrange,         // Text on a warm orange background is black.
    secondary = White,         // Use white for secondary elements.
    onSecondary = Black,
    background = Black,        // Dark background.
    onBackground = White,
    surface = Black,           // Cards and surfaces are dark.
    onSurface = White
)

// Light mode
private val LightColorScheme = lightColorScheme(
    primary = White,      // Accent color remains warm orange.
    onPrimary = Black,         // White text on warm orange.
    secondary = Black,         // Black used for secondary elements.
    onSecondary = White,
    background = White,        // Light background.
    onBackground = Black,
    surface = Color(0xFFD9D9D9),           // Cards and surfaces are light.
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
                    primary = Color(0xFF2A2929),      // Accent color remains the warm orange.
                    onPrimary = White,
                    background = Black,
                    onBackground = White,
                    surface = Color( 0xFF2A2929), // Darker surface color
                    onSurface = White
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    primary = White,
                    onPrimary = Black,
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

    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor     = WarmOrange,
            backgroundColor = WarmOrange.copy(alpha = 0.4f)
        ),
        LocalAppCursorColor provides WarmOrange
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AppTypography,
            content     = content
        )
    }
}