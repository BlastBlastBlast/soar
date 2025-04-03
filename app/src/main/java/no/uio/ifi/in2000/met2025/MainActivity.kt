package no.uio.ifi.in2000.met2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.met2025.ui.navigation.AppNavLauncher
import no.uio.ifi.in2000.met2025.ui.theme.In2000_met2025_team21Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Default to light mode.
            var darkTheme by remember { mutableStateOf(false) }
            In2000_met2025_team21Theme(darkTheme = darkTheme) {
                AppNavLauncher(
                    darkTheme = darkTheme,
                    toggleTheme = {
                        darkTheme = !darkTheme
                        // Optionally, update the system theme using AppCompatDelegate:
                        AppCompatDelegate.setDefaultNightMode(
                            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
                            else AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                )
            }
        }
    }
}