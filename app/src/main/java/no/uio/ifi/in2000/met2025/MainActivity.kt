package no.uio.ifi.in2000.met2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.met2025.ui.theme.SOAR_Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark = remember { mutableStateOf(true) }
            SOAR_Theme(darkTheme = isDark.value) {
                App(
                    darkTheme  = isDark.value,
                    toggleTheme = {
                        isDark.value = !isDark.value
                        AppCompatDelegate.setDefaultNightMode(
                            if (isDark.value)
                                AppCompatDelegate.MODE_NIGHT_YES
                            else
                                AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                )
            }
        }
    }
}
