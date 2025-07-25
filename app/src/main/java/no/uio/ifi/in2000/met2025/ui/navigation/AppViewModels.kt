package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.MapScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.WeatherViewModel

/**
 * A simple holder for all of your top-level navigation view-models.
 */
data class AppViewModels(
    val maps: MapScreenViewModel,
    val weather: WeatherViewModel,
    val configs: ConfigViewModel
)

/**
 * Call once in a @Composable scope to fetch all four Hilt VMs at once.
 */
@Composable
fun provideAppViewModels(): AppViewModels {
    val maps       = hiltViewModel<MapScreenViewModel>()
    val weather    = hiltViewModel<WeatherViewModel>()
    val configs   = hiltViewModel<ConfigViewModel>()

    return AppViewModels(
        maps = maps,
        weather = weather,
        configs = configs,
    )
}