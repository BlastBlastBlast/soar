package no.uio.ifi.in2000.met2025.ui.screens.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.WeatherConfigRepository
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val weatherRepo: WeatherConfigRepository,
    private val rocketRepo: RocketConfigRepository
) : ViewModel() {

    //–– shared UpdateStatus type ––
    sealed class UpdateStatus {
        object Idle : UpdateStatus()
        object Success : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    //–– 1) LIST FLOWS ––
    val weatherConfigs: Flow<List<WeatherConfig>> = weatherRepo.getAllConfigProfiles()
    val rocketConfigs:  Flow<List<RocketConfig>>  = rocketRepo.getAllRocketConfigs()

    //–– 2) NAME LISTS FOR DUPLICATE‐CHECKS ––
    private val _weatherNames = MutableStateFlow<List<String>>(emptyList())
    private val _rocketNames  = MutableStateFlow<List<String>>(emptyList())
    val weatherNames: StateFlow<List<String>> = _weatherNames
    val rocketNames:  StateFlow<List<String>> = _rocketNames

    init {
        viewModelScope.launch {
            weatherRepo.getAllConfigProfileNames()
                .collect { _weatherNames.value = it }
        }
        viewModelScope.launch {
            rocketRepo.getAllRocketConfigNames()
                .collect { _rocketNames.value = it }
        }
    }

    //–– 3) INDIVIDUAL LOADS ––
    fun getWeatherConfig(id: Int): Flow<WeatherConfig?> = weatherRepo.getWeatherConfig(id)
    fun getRocketConfig(id: Int):  Flow<RocketConfig?>  = rocketRepo.getRocketConfig(id)

    //–– 4) WEATHER “updateStatus” ––
    private val _updateStatus            = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    fun saveWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.insertConfigProfile(cfg)
        _updateStatus.value = UpdateStatus.Success
    }

    fun updateWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.updateConfigProfile(cfg)
        _updateStatus.value = UpdateStatus.Success
    }

    fun deleteWeatherConfig(cfg: WeatherConfig) = viewModelScope.launch {
        weatherRepo.deleteConfigProfile(cfg)
    }

    fun checkWeatherNameAvailability(name: String) {
        _updateStatus.value = if (_weatherNames.value.contains(name)) {
            UpdateStatus.Error("A config named \"$name\" already exists")
        } else {
            UpdateStatus.Idle
        }
    }

    fun resetWeatherStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    //–– 5) ROCKET “rocketUpdateStatus” ––
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

    fun checkRocketNameAvailability(name: String) {
        _rocketUpdateStatus.value = if (_rocketNames.value.contains(name)) {
            UpdateStatus.Error("A rocket config named \"$name\" already exists")
        } else {
            UpdateStatus.Idle
        }
    }

    fun resetRocketStatus() {
        _rocketUpdateStatus.value = UpdateStatus.Idle
    }
}
