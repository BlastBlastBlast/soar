package no.uio.ifi.in2000.met2025.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.R

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White, // ensures text on dark backgrounds is white
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black, // ensures text on light backgrounds is dark
    onSurface = Color.Black
)

// Define your JetBrains Mono Nerd FontFamily.
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_nerd_regular, FontWeight.Normal)
)

// Create custom typography by copying the default typography and updating each style.
val AppTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = JetBrainsMono),
    displayMedium = Typography().displayMedium.copy(fontFamily = JetBrainsMono),
    displaySmall = Typography().displaySmall.copy(fontFamily = JetBrainsMono),
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = JetBrainsMono),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = JetBrainsMono),
    titleLarge = Typography().titleLarge.copy(fontFamily = JetBrainsMono),
    titleMedium = Typography().titleMedium.copy(fontFamily = JetBrainsMono),
    titleSmall = Typography().titleSmall.copy(fontFamily = JetBrainsMono),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = JetBrainsMono),
    bodySmall = Typography().bodySmall.copy(fontFamily = JetBrainsMono),
    labelLarge = Typography().labelLarge.copy(fontFamily = JetBrainsMono),
    labelMedium = Typography().labelMedium.copy(fontFamily = JetBrainsMono),
    labelSmall = Typography().labelSmall.copy(fontFamily = JetBrainsMono)
)

@Composable
fun In2000_met2025_team21Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // keep dynamic color if you want
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                // Override the dynamic dark scheme with white text colors.
                dynamicDarkColorScheme(context).copy(
                    onBackground = Color.White,
                    onSurface = Color.White,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onTertiary = Color.White
                )
            } else {
                dynamicLightColorScheme(context)
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

