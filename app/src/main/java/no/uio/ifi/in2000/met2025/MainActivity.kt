package no.uio.ifi.in2000.met2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.met2025.ui.navigation.AppNavLauncher
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.AtmosphericWindScreen
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.AtmosphericWindViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavLauncher()
        }
    }
}