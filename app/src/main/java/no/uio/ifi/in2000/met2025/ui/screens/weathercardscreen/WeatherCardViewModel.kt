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
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.AtmosphericWindViewModel.AtmosphericWindUiState
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DefaultConfig
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class WeatherCardViewmodel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val configProfileRepository: ConfigProfileRepository,
    private val launchSitesRepository: LaunchSitesRepository,
    private val weatherModel: WeatherModel
) : ViewModel() {

    sealed class WeatherCardUiState {
        data object Idle : WeatherCardUiState()
        data object Loading : WeatherCardUiState()
        data class Success(val forecastItems: List<ForecastDataItem>) : WeatherCardUiState()
        data class Error(val message: String) : WeatherCardUiState()
    }

    sealed class AtmosphericWindUiState {
        data object Idle : AtmosphericWindUiState()
        data object Loading : AtmosphericWindUiState()
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

    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    private val _isobaricData = MutableStateFlow<Map<Instant, AtmosphericWindUiState>>(emptyMap())
    val isobaricData: StateFlow<Map<Instant,AtmosphericWindUiState>> = _isobaricData

    init {
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
        viewModelScope.launch {
            launchSitesRepository.getTempSite().collect { site ->
                site?.let {
                    _coordinates.value = Pair(it.latitude, it.longitude)
                }
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
                    _uiState.value = WeatherCardUiState.Error(throwable.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadIsobaricData(lat: Double, lon: Double, time: Instant) {
        viewModelScope.launch {
            //Mutex().withLock {
            updateIsobaricData(lat, lon, time)
            //}
        }
    }

    private suspend fun updateIsobaricData(
        lat: Double,
        lon: Double,
        time: Instant
    ) {
        val currentItem = isobaricData.value[time]
        _isobaricData.value += (time to AtmosphericWindUiState.Loading)
        _isobaricData.value += (
                time to weatherModel.getCurrentIsobaricData(lat, lon, time).fold(
                    onFailure = { throwable ->
                        if (currentItem !is AtmosphericWindUiState.Success)
                            AtmosphericWindUiState.Error(
                                throwable.message ?: "Ukjent feil"
                            )
                        else
                            AtmosphericWindUiState.Success(currentItem.isobaricData) //TODO: add something on screen to show that this value is outdated
                    },
                    onSuccess = { data ->
                        AtmosphericWindUiState.Success(data)
                    }
                )
                )
    }
}
