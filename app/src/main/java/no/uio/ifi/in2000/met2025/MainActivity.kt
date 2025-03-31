package no.uio.ifi.in2000.met2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.ui.navigation.AppNavLauncher
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen

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
//val client = HttpClient(CIO) {
//    install(Logging) {
//        level = LogLevel.HEADERS // Only headers for binary files
//    }
//    expectSuccess = false
//}
//    val isoDS = IsobaricDataSource(client)
//    val isoRep = IsobaricRepository(isoDS)
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Run fetchCurrentIsobaricgribData inside a coroutine
//        lifecycleScope.launch {
//            val test = isoRep.getCurrentIsobaricGribData()
//            println("Data: \n$test")  // Print or use the fetched data
//        }
//    }
//}