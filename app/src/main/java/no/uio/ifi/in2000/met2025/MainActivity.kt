package no.uio.ifi.in2000.met2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.met2025.ui.navigation.AppNavLauncher

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