package no.uio.ifi.in2000.met2025.ui.screens.config

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.UserPreferences
import no.uio.ifi.in2000.met2025.data.local.configprofiles.WeatherConfigRepository
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

/**
 * ConfigViewModel
 *
 * Manages UI state for weather and rocket configuration screens.
 */
@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val weatherRepo: WeatherConfigRepository,
    private val rocketRepo: RocketConfigRepository,
    private val userPrefs: UserPreferences
) : ViewModel() {

    sealed class UpdateStatus {
        object Idle : UpdateStatus()
        object Success : UpdateStatus()
    }

    val weatherConfigs: Flow<List<WeatherConfig>> = weatherRepo.getAllWeatherConfigs()
    val rocketConfigs:  Flow<List<RocketConfig>>  = rocketRepo.getAllRocketConfigs()

    private val _weatherNames = MutableStateFlow<List<String>>(emptyList())
    private val _rocketNames  = MutableStateFlow<List<String>>(emptyList())
    val weatherNames: StateFlow<List<String>> = _weatherNames
    val rocketNames:  StateFlow<List<String>> = _rocketNames

    init {
        viewModelScope.launch {
            weatherRepo.getAllWeatherConfigNames()
                .collect { _weatherNames.value = it }
        }
        viewModelScope.launch {
            rocketRepo.getAllRocketConfigNames()
                .collect { _rocketNames.value = it }
        }
    }

    fun getWeatherConfig(id: Int): Flow<WeatherConfig?> = weatherRepo.getWeatherConfig(id)
    fun getRocketConfig(id: Int):  Flow<RocketConfig?>  = rocketRepo.getRocketConfig(id)

    private val _updateStatus            = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    val isRocketConfigFirstRun = userPrefs.isRocketConfigFirstRunFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun saveWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.insertWeatherConfig(cfg)
        _updateStatus.value = UpdateStatus.Success
    }

    fun updateWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.updateWeatherConfig(cfg)
        _updateStatus.value = UpdateStatus.Success
    }

    fun deleteWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.deleteWeatherConfig(cfg)
    }

    fun resetWeatherStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    private val _rocketUpdateStatus            = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val rocketUpdateStatus: StateFlow<UpdateStatus> = _rocketUpdateStatus

    fun saveRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.insertRocketConfig(rc)
        _rocketUpdateStatus.value = UpdateStatus.Success
    }

    fun updateRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.updateRocketConfig(rc)
        _rocketUpdateStatus.value = UpdateStatus.Success
    }

    fun deleteRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.deleteRocketConfig(rc)
    }

    fun resetRocketStatus() {
        _rocketUpdateStatus.value = UpdateStatus.Idle
    }

    fun markRocketConfigSeen() = viewModelScope.launch {
        userPrefs.markRocketConfigSeen()
    }
}
