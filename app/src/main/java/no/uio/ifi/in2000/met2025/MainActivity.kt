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
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
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



/*
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.HEADERS // Only headers for binary files
        }
        expectSuccess = false
    }
    val isoDS = IsobaricDataSource(client)
    val isoRep = IsobaricRepository(isoDS)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Run fetchCurrentIsobaricgribData inside a coroutine
        lifecycleScope.launch {
            val test = isoRep.getCurrentIsobaricGribData()
            for ((latLon, isobaricMap) in test) {
                println("Coordinates: $latLon")
                //for ((pressure, data) in isobaricMap) {
                //    println("  Pressure Level: $pressure hPa")
                //    println("    Temperature: ${data.temperature} K")
                //    println("    U-Wind: ${data.uComponentWind} m/s")
                //    println("    V-Wind: ${data.vComponentWind} m/s")
                }
            } // Print or use the fetched data
        }
    }
*/