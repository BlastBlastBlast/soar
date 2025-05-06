package no.uio.ifi.in2000.met2025.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val weatherRepo: ConfigProfileRepository,
    private val rocketRepo: RocketConfigRepository
) : ViewModel() {

    // 1) — LIST FLOWS —
    val weatherConfigs: Flow<List<ConfigProfile>> = weatherRepo.getAllConfigProfiles()
    val rocketConfigs:  Flow<List<RocketConfig>> = rocketRepo.getAllRocketConfigs()

    // 2) — NAME LISTS FOR DUPLICATE CHECKS —
    private val _weatherNames = MutableStateFlow<List<String>>(emptyList())
    private val _rocketNames  = MutableStateFlow<List<String>>(emptyList())
    val weatherNames: StateFlow<List<String>> = _weatherNames
    val rocketNames:  StateFlow<List<String>> = _rocketNames

    init {
        viewModelScope.launch {
            weatherRepo.getAllConfigProfileNames().collect { _weatherNames.value = it }
        }
        viewModelScope.launch {
            rocketRepo.getAllRocketConfigNames().collect { _rocketNames.value = it }
        }
    }

    // 3) — INDIVIDUAL LOADS —
    fun getWeatherConfig(id: Int): Flow<ConfigProfile?> = weatherRepo.getConfigProfile(id)
    fun getRocketConfig(id: Int):  Flow<RocketConfig?>  = rocketRepo.getRocketConfig(id)

    // 4) — EDIT STATUS —
    sealed class EditStatus {
        object Idle    : EditStatus()
        object Success : EditStatus()
        data class Error(val message: String) : EditStatus()
    }

    private val _weatherEditStatus = MutableStateFlow<EditStatus>(EditStatus.Idle)
    private val _rocketEditStatus  = MutableStateFlow<EditStatus>(EditStatus.Idle)
    val weatherEditStatus: StateFlow<EditStatus> = _weatherEditStatus
    val rocketEditStatus:  StateFlow<EditStatus> = _rocketEditStatus

    // 5) — WEATHER CRUD —
    fun saveWeatherConfig(cfg: ConfigProfile) = viewModelScope.launch {
        weatherRepo.insertConfigProfile(cfg)
        _weatherEditStatus.value = EditStatus.Success
    }

    fun updateWeatherConfig(cfg: ConfigProfile) = viewModelScope.launch {
        weatherRepo.updateConfigProfile(cfg)
        _weatherEditStatus.value = EditStatus.Success
    }

    fun deleteWeatherConfig(cfg: ConfigProfile) = viewModelScope.launch {
        weatherRepo.deleteConfigProfile(cfg)
    }

    fun checkWeatherNameAvailability(name: String) {
        _weatherEditStatus.value = if (_weatherNames.value.contains(name)) {
            EditStatus.Error("A weather config named \"$name\" already exists")
        } else {
            EditStatus.Idle
        }
    }

    fun resetWeatherStatus() {
        _weatherEditStatus.value = EditStatus.Idle
    }

    // 6) — ROCKET CRUD —
    fun saveRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.insertRocketConfig(rc)
        _rocketEditStatus.value = EditStatus.Success
    }

    fun updateRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.updateRocketConfig(rc)
        _rocketEditStatus.value = EditStatus.Success
    }

    fun deleteRocketConfig(rc: RocketConfig) = viewModelScope.launch {
        rocketRepo.deleteRocketConfig(rc)
    }

    fun checkRocketNameAvailability(name: String) {
        _rocketEditStatus.value = if (_rocketNames.value.contains(name)) {
            EditStatus.Error("A rocket config named \"$name\" already exists")
        } else {
            EditStatus.Idle
        }
    }

    fun resetRocketStatus() {
        _rocketEditStatus.value = EditStatus.Idle
    }
}