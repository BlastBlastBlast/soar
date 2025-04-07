package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DefaultConfig
import javax.inject.Inject


@HiltViewModel
class WeatherCardViewmodel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val configProfileDao: ConfigProfileDAO
) : ViewModel() {

    sealed class WeatherCardUiState {
        object Idle : WeatherCardUiState()
        object Loading : WeatherCardUiState()
        data class Success(val forecastItems: List<ForecastDataItem>) : WeatherCardUiState()
        data class Error(val message: String) : WeatherCardUiState()
    }

    private val _uiState = MutableStateFlow<WeatherCardUiState>(WeatherCardUiState.Idle)
    val uiState: StateFlow<WeatherCardUiState> = _uiState

    // Hold the active configuration
    private val _activeConfig = MutableStateFlow<ConfigProfile?>(null)
    val activeConfig: StateFlow<ConfigProfile?> = _activeConfig

    // Expose a list of all config profiles from the database
    private val _configList = MutableStateFlow<List<ConfigProfile>>(emptyList())
    val configList: StateFlow<List<ConfigProfile>> = _configList

    init {
        viewModelScope.launch {
            configProfileDao.getDefaultConfigProfile().collect { defaultConfig ->
                if (defaultConfig != null) {
                    _activeConfig.value = defaultConfig
                } else {
                    _activeConfig.value = DefaultConfig.instance
                }
            }
        }
        // Collect the full list of configurations.
        viewModelScope.launch {
            configProfileDao.getAllConfigProfiles().collect { list ->
                _configList.value = list
            }
        }
    }

    fun setActiveConfig(config: ConfigProfile) {
        _activeConfig.value = config
    }

    fun loadForecast(lat: Double, lon: Double, timeSpanInHours: Int = 72) {
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
