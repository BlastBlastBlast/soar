package no.uio.ifi.in2000.met2025.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.domain.ForecastDataMapper
import javax.inject.Inject

@HiltViewModel
class WeatherCardViewmodel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val forecastDataMapper: ForecastDataMapper
) : ViewModel() {

    // Data stored as maps keyed by timestamp
    data class ForecastDisplayData(
        val temperatures: Map<String, Double>,
        val humidities: Map<String, Double>,
        val windSpeeds: Map<String, Double>,
        val windGusts: Map<String, Double>,
        val windDirections: Map<String, Double>,
        val precipitations: Map<String, Double>,
        val visibilities: Map<String, Double>,
        val dewPoints: Map<String, Double>,
        val cloudCovers: Map<String, Double>,
        val thunderProbabilities: Map<String, Double>
    )

    sealed class WeatherCardUiState {
        object Idle : WeatherCardUiState()
        object Loading : WeatherCardUiState()
        data class Success(val displayData: ForecastDisplayData) : WeatherCardUiState()
        data class Error(val message: String) : WeatherCardUiState()
    }

    private val _uiState = MutableStateFlow<WeatherCardUiState>(WeatherCardUiState.Idle)
    val uiState: StateFlow<WeatherCardUiState> = _uiState

    fun loadForecast(lat: Double, lon: Double, timeSpanInHours: Int = 4) {
        viewModelScope.launch {
            _uiState.value = WeatherCardUiState.Loading
            val result = locationForecastRepository.getForecastData(lat, lon, timeSpanInHours)
            result.fold(
                onSuccess = { forecastData ->
                    val displayData = forecastDataMapper.mapForecastDataToDisplayData(forecastData)
                    _uiState.value = WeatherCardUiState.Success(displayData)
                },
                onFailure = { throwable ->
                    _uiState.value = WeatherCardUiState.Error(throwable.message ?: "Unknown error")
                }
            )
        }
    }
}