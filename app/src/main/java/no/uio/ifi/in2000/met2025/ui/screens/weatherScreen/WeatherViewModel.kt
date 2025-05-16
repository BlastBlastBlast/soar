package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.WeatherConfigRepository
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSiteRepository
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricDataResult
import no.uio.ifi.in2000.met2025.data.models.sunrise.ValidSunTimes
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherConfigOverlay.DefaultWeatherParameters
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel responsible for managing weather-related UI state and data operations.
 * Handles loading of forecast data, isobaric wind layers, sun times, config profiles, and coordinates.
 * Uses dependency-injected repositories and exposes reactive StateFlows to the UI layer.
 *
 */


@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val weatherConfigRepository: WeatherConfigRepository,
    private val launchSiteRepository: LaunchSiteRepository,
    private val weatherModel: WeatherModel,
    private val sunriseRepository: SunriseRepository,
    private val isobaricRepository: IsobaricRepository
) : ViewModel() {

    // UI state sealed class for regular weather data
    sealed class WeatherUiState {
        object Idle : WeatherUiState()
        object Loading : WeatherUiState()
        data class Success(
            val forecastItems: List<ForecastDataItem>,
            val sunTimes: Map<String, ValidSunTimes> = emptyMap()
        ) : WeatherUiState()
        data class Error(val message: String) : WeatherUiState()
    }

    // UI state sealed class for wind layer/isobaric data
    sealed class AtmosphericWindUiState {
        object Idle : AtmosphericWindUiState()
        object Loading : AtmosphericWindUiState()
        data class Success(val isobaricData: IsobaricData) : AtmosphericWindUiState()
        data class Error(val message: String) : AtmosphericWindUiState()
    }

    // UI states
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState


    private val _windState = MutableStateFlow<AtmosphericWindUiState>(AtmosphericWindUiState.Idle)
    val windState: StateFlow<AtmosphericWindUiState> = _windState

    // Weather config state
    private val _activeConfig = MutableStateFlow<WeatherConfig?>(null)
    val activeConfig: StateFlow<WeatherConfig?> = _activeConfig

    private val _configList = MutableStateFlow<List<WeatherConfig>>(emptyList())
    val configList: StateFlow<List<WeatherConfig>> = _configList

    // Default coordinates are used if no temp site exists.
    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    // Track the coordinates for which the isobaric data was last fetched.
    private val _lastIsobaricCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val lastIsobaricCoordinates: StateFlow<Pair<Double, Double>?> = _lastIsobaricCoordinates

    private val _isobaricData = MutableStateFlow<Map<Instant, AtmosphericWindUiState>>(emptyMap())
    val isobaricData: StateFlow<Map<Instant, AtmosphericWindUiState>> = _isobaricData

    private val _latestAvailableGribTime = MutableStateFlow<Instant?>(null)
    val latestAvailableGribTime: StateFlow<Instant?> = _latestAvailableGribTime

    val validSunTimesMap = mutableMapOf<String, ValidSunTimes>()

    private val _currentSite: StateFlow<LaunchSite?> =
        launchSiteRepository.getActiveSite().stateIn(
            viewModelScope, SharingStarted.Eagerly, null
        )
    val currentSite: StateFlow<LaunchSite?> = _currentSite

    private val _launchSites = MutableStateFlow<List<LaunchSite>>(emptyList())
    val launchSites: StateFlow<List<LaunchSite>> = _launchSites

    init {
        // Initialize configuration profiles.
        viewModelScope.launch {
            val currentConfigs = weatherConfigRepository.getAllWeatherConfigs().first()
            if (currentConfigs.none { it.isDefault }) {
                weatherConfigRepository.insertWeatherConfig(DefaultWeatherParameters.instance)
            }
        }
        viewModelScope.launch {
            weatherConfigRepository.getDefaultWeatherConfig().collect { defaultConfig ->
                _activeConfig.value = defaultConfig ?: DefaultWeatherParameters.instance
            }
        }
        viewModelScope.launch {
            weatherConfigRepository.getAllWeatherConfigs().collect { list ->
                _configList.value = list
            }
        }
        // Continuously collect the current coordinates from the repository.
        viewModelScope.launch {
            launchSiteRepository
                .getCurrentCoordinates(defaultCoordinates = Pair(59.942, 10.726))
                .collect { newCoordinates ->
                    _coordinates.value = newCoordinates
                    // Optionally, if you want to automatically clear the last loaded isobaric data when a new location is set:
                    // if (_lastIsobaricCoordinates.value != newCoordinates) {
                    //     // Clear the isobaric data for all times or for your current time key.
                    // }
                }
        }
        // Continuously collect the latest available GRIB time from the repository.
        viewModelScope.launch {
            isobaricRepository.getLatestAvailableGribFlow().collect { time ->
                _latestAvailableGribTime.value = time
            }
        }
        viewModelScope.launch {
            launchSiteRepository.getAll().collect { list ->
                _launchSites.value = list
            }
        }
    }

    fun clearIsobaricDataForTime(time: Instant) {
        // Clear the isobaric data state for the given forecast time.
        _isobaricData.value = _isobaricData.value.toMutableMap().apply {
            put(time, AtmosphericWindUiState.Idle)
        }
    }

    fun updateCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Use the repository function to get the "Last Visited" site.
            val lastVisitedSite = launchSiteRepository.getLastVisitedTempSite().first()
            if (lastVisitedSite != null) {
                // Overwrite by updating the existing record.
                val updatedSite = lastVisitedSite.copy(latitude = lat, longitude = lon)
                launchSiteRepository.update(updatedSite)
            } else {
                // If not found, insert a new record.
                launchSiteRepository.insert(
                    LaunchSite(
                        name = "Last Visited",
                        latitude = lat,
                        longitude = lon
                    )
                )
            }
        }
    }


    fun setActiveConfig(config: WeatherConfig) {
        _activeConfig.value = config
    }

    // Fetch forecast data and matching sun times
    fun loadForecast(lat: Double, lon: Double, timeSpanInHours: Int = 120) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val result = locationForecastRepository.getTimeZoneAdjustedForecast(lat, lon, timeSpanInHours)
            result.fold(
                onSuccess = { forecastData ->
                    val forecastItems = forecastData.timeSeries

                    val dates = forecastItems
                        .map { it.time.substring(0, 10) }
                        .distinct()

                    val sunTimesMap = mutableMapOf<String, ValidSunTimes>()

                    for (date in dates) {
                        val sunTimes = sunriseRepository.getValidSunTimes(lat, lon, date)
                        sunTimesMap[date] = sunTimes
                    }

                    _uiState.value = WeatherUiState.Success(
                        forecastItems = forecastItems,
                        sunTimes = sunTimesMap
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = WeatherUiState.Error(
                        throwable.message ?: ("Error fetching forecast data." +
                                "Please check your internet connection and try again.")
                    )
                }
            )
        }
    }

    // Trigger loading of isobaric (wind layer) data
    fun loadIsobaricData(lat: Double, lon: Double, time: Instant) {
        viewModelScope.launch {
            _lastIsobaricCoordinates.value = Pair(lat, lon)
            updateIsobaricData(lat, lon, time)
        }
    }

    // Internal function to fetch and store isobaric wind data
    private suspend fun updateIsobaricData(
        lat: Double,
        lon: Double,
        time: Instant
    ) {
        val currentItem = isobaricData.value[time]

        _isobaricData.value += (time to AtmosphericWindUiState.Loading)

        val result = weatherModel.getIsobaricData(lat, lon, time)

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
            IsobaricDataResult.DataParsingError -> {
                AtmosphericWindUiState.Error("Error while parsing data")
            }
            IsobaricDataResult.OutOfBoundsError -> {
                AtmosphericWindUiState.Error("Coordinates are out of bounds. GRIB bounds are 64.25N, -1.45W, 55.35S, 14.51E." +
                        "Some rounding exceptions may apply for border limits.")
            }
        }
        _isobaricData.value += (time to newState)
    }

    // Fetch and cache sun times for current and upcoming days
    suspend fun getValidSunTimesList(lat: Double, lon: Double) {
        val date = Instant.now()
            .atZone(ZoneId.of("Europe/Oslo"))
            .toLocalDate()

        for (i in 0..3) { // Only 4 days, not 5! (careful here)
            val currentDate = date.plusDays(i.toLong())
            val key = "${lat}_${lon}_${currentDate}" // ✅ Unique key

            val sunTimes = sunriseRepository.getValidSunTimes(
                lat, lon, currentDate.toString()
            )
            validSunTimesMap[key] = sunTimes
        }
    }
}
