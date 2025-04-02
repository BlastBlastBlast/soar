package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import javax.inject.Inject

@HiltViewModel
class WeatherCardViewmodel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository
) : ViewModel() {

    sealed class WeatherCardUiState {
        object Idle : WeatherCardUiState()
        object Loading : WeatherCardUiState()
        data class Success(val forecastItems: List<ForecastDataItem>) : WeatherCardUiState()
        data class Error(val message: String) : WeatherCardUiState()
    }

    private val _uiState = MutableStateFlow<WeatherCardUiState>(WeatherCardUiState.Idle)
    val uiState: StateFlow<WeatherCardUiState> = _uiState

    fun loadForecast(lat: Double, lon: Double, timeSpanInHours: Int = 72) { //shows 10 weather cards
        viewModelScope.launch {
            _uiState.value = WeatherCardUiState.Loading
            val result = locationForecastRepository.getForecastData(lat, lon, timeSpanInHours)
            result.fold(
                onSuccess = { forecastData ->
                    _uiState.value = WeatherCardUiState.Success(forecastData.timeSeries)
                },
                onFailure = { throwable ->
                    _uiState.value =
                        WeatherCardUiState.Error(throwable.message ?: "Unknown error")
                }
            )
        }
    }
}
