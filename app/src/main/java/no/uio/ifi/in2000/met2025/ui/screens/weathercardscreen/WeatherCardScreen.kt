package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.components.HourlyExpandableCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import no.uio.ifi.in2000.met2025.ui.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.components.DailyForecastCard
import no.uio.ifi.in2000.met2025.ui.components.DailyForecastRowSection


@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coordinates by locationViewModel.coordinates.collectAsState()

    LaunchedEffect(coordinates) {
        viewModel.loadForecast(coordinates.first, coordinates.second)
    }

    ScreenContent(uiState = uiState)
}

@Composable
fun ScreenContent(
    uiState: WeatherCardViewmodel.WeatherCardUiState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        when (uiState) {
            is WeatherCardViewmodel.WeatherCardUiState.Loading -> {
                Text("Loading...", style = MaterialTheme.typography.headlineSmall)
            }
            is WeatherCardViewmodel.WeatherCardUiState.Error -> {
                Text("Error: ${uiState.message}", style = MaterialTheme.typography.headlineSmall)
            }
            is WeatherCardViewmodel.WeatherCardUiState.Success -> {

                val forecastItems = uiState.forecastItems

                // 👇 Plasser LazyRow med tre dagers kort her
                DailyForecastRowSection(forecastItems = forecastItems)

                // 👇 Timevis visning etterpå
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                val today = uiState.forecastItems.firstOrNull()?.time?.substring(0, 10)
                val dailyItems = uiState.forecastItems.filter { it.time.startsWith(today ?: "") }


                /*DailyForecastCard(
                    forecastItems = dailyItems,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(text = "Hourly Forecast",style = MaterialTheme.typography.titleLarge) */

                //  Vis detaljer time for time
                dailyItems.forEach { forecastItem ->
                    HourlyExpandableCard(
                        forecastItem = forecastItem,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            else -> Unit
        }
    }
}
