package no.uio.ifi.in2000.met2025

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.met2025.ui.home.WeatherCardScreen
import no.uio.ifi.in2000.met2025.ui.theme.In2000_met2025_team21Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            In2000_met2025_team21Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    WeatherCardScreen(
                        viewModel = TODO()
                    )
                }
                }
            }
        }
    }