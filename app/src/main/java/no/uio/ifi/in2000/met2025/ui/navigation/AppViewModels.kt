package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.configprofiles.ConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.RocketConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

/**
 * A simple holder for all of your top-level navigation view-models.
 */
data class AppViewModels(
    val home: HomeScreenViewModel,
    val weather: WeatherCardViewmodel,
    val settings: SettingsViewModel
)

/**
 * Call once in a @Composable scope to fetch all four Hilt VMs at once.
 */
@Composable
fun provideAppViewModels(): AppViewModels {
    val home       = hiltViewModel<HomeScreenViewModel>()
    val weather    = hiltViewModel<WeatherCardViewmodel>()
    val settings   = hiltViewModel<SettingsViewModel>()

    return AppViewModels(
        home = home,
        weather = weather,
        settings = settings,
    )
}