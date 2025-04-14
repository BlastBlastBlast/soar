package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataResult
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DefaultConfig
import java.time.Instant
import javax.inject.Inject

// WeatherCardViewModel.kt
@HiltViewModel
class WeatherCardViewmodel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val configProfileRepository: ConfigProfileRepository,
    private val launchSitesRepository: LaunchSitesRepository,
    private val weatherModel: WeatherModel
) : ViewModel() {

    sealed class WeatherCardUiState {
        object Idle : WeatherCardUiState()
        object Loading : WeatherCardUiState()
        data class Success(val forecastItems: List<ForecastDataItem>) : WeatherCardUiState()
        data class Error(val message: String) : WeatherCardUiState()
    }

    sealed class AtmosphericWindUiState {
        object Idle : AtmosphericWindUiState()
        object Loading : AtmosphericWindUiState()
        data class Success(val isobaricData: IsobaricData) : AtmosphericWindUiState()
        data class Error(val message: String) : AtmosphericWindUiState()
    }

    private val _uiState = MutableStateFlow<WeatherCardUiState>(WeatherCardUiState.Idle)
    val uiState: StateFlow<WeatherCardUiState> = _uiState

    private val _windState = MutableStateFlow<AtmosphericWindUiState>(AtmosphericWindUiState.Idle)
    val windState: StateFlow<AtmosphericWindUiState> = _windState

    private val _activeConfig = MutableStateFlow<ConfigProfile?>(null)
    val activeConfig: StateFlow<ConfigProfile?> = _activeConfig

    private val _configList = MutableStateFlow<List<ConfigProfile>>(emptyList())
    val configList: StateFlow<List<ConfigProfile>> = _configList

    // Default coordinates are used if no temp site exists.
    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    // Track the coordinates for which the isobaric data was last fetched.
    private val _lastIsobaricCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val lastIsobaricCoordinates: StateFlow<Pair<Double, Double>?> = _lastIsobaricCoordinates

    private val _isobaricData = MutableStateFlow<Map<Instant, AtmosphericWindUiState>>(emptyMap())
    val isobaricData: StateFlow<Map<Instant, AtmosphericWindUiState>> = _isobaricData

    init {
        // Initialize configuration profiles.
        viewModelScope.launch {
            val currentConfigs = configProfileRepository.getAllConfigProfiles().first()
            if (currentConfigs.none { it.isDefault }) {
                configProfileRepository.insertConfigProfile(DefaultConfig.instance)
            }
        }
        viewModelScope.launch {
            configProfileRepository.getDefaultConfigProfile().collect { defaultConfig ->
                _activeConfig.value = defaultConfig ?: DefaultConfig.instance
            }
        }
        viewModelScope.launch {
            configProfileRepository.getAllConfigProfiles().collect { list ->
                _configList.value = list
            }
        }
        // Continuously collect the current coordinates from the repository.
        viewModelScope.launch {
            launchSitesRepository
                .getCurrentCoordinates(tempName = "Last Visited", defaultCoordinates = Pair(59.942, 10.726))
                .collect { newCoordinates ->
                    _coordinates.value = newCoordinates
                    // Optionally, if you want to automatically clear the last loaded isobaric data when a new location is set:
                    // if (_lastIsobaricCoordinates.value != newCoordinates) {
                    //     // Clear the isobaric data for all times or for your current time key.
                    // }
                }
        }
    }

    fun clearIsobaricDataForTime(time: Instant) {
        // Clear the isobaric data state for the given forecast time.
        _isobaricData.value = _isobaricData.value.toMutableMap().apply {
            put(time, AtmosphericWindUiState.Idle)
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
                    _uiState.value = WeatherCardUiState.Error(throwable.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadIsobaricData(lat: Double, lon: Double, time: Instant) {
        viewModelScope.launch {
            _lastIsobaricCoordinates.value = Pair(lat, lon)
            updateIsobaricData(lat, lon, time)
        }
    }

    private suspend fun updateIsobaricData(
        lat: Double,
        lon: Double,
        time: Instant
    ) {
        val currentItem = isobaricData.value[time]

        _isobaricData.value += (time to AtmosphericWindUiState.Loading)

        val result = weatherModel.getCurrentIsobaricData(lat, lon, time)

        val newState: AtmosphericWindUiState = when (result) {
            is IsobaricDataResult.Success -> {
                result.isobaricData.fold(
                    onSuccess = { data -> AtmosphericWindUiState.Success(data) },
                    onFailure = { error ->
                        if (currentItem is AtmosphericWindUiState.Success) {
                            currentItem // preserve existing success data
                        } else {
                            AtmosphericWindUiState.Error(error.message ?: "Unknown error while parsing data")
                        }
                    }
                )
            }

            IsobaricDataResult.GribAvailabilityError -> {
                AtmosphericWindUiState.Error("No GRIB data available for the given time")
            }

            IsobaricDataResult.GribFetchingError -> {
                AtmosphericWindUiState.Error("Error while fetching GRIB data")
            }
            //TODO: Separere denne fra gribupdate kallet hvis man gidder
            IsobaricDataResult.LocationForecastFetchingError -> {
                AtmosphericWindUiState.Error("Error while fetching location forecast data")
            }
        }

        _isobaricData.value += (time to newState)
    }
}
