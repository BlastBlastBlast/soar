package no.uio.ifi.in2000.met2025.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
): ViewModel() {
    // Flows
    val weatherConfigs = weatherRepo.getAllConfigProfiles()
    val rocketConfigs  = rocketRepo.getAllRocketConfigs()

    // Weather ops
    fun deleteWeatherConfig(cfg: ConfigProfile) =
        viewModelScope.launch { weatherRepo.deleteConfigProfile(cfg) }
    fun saveWeatherConfig(cfg: ConfigProfile) =
        viewModelScope.launch { weatherRepo.insertConfigProfile(cfg) }
    fun updateWeatherConfig(cfg: ConfigProfile) =
        viewModelScope.launch { weatherRepo.updateConfigProfile(cfg) }

    // Rocket ops
    fun deleteRocketConfig(rc: RocketConfig) =
        viewModelScope.launch { rocketRepo.deleteRocketConfig(rc) }
    fun saveRocketConfig(rc: RocketConfig) =
        viewModelScope.launch { rocketRepo.insertRocketConfig(rc) }
    fun updateRocketConfig(rc: RocketConfig) =
        viewModelScope.launch { rocketRepo.updateRocketConfig(rc) }

    // Individual getters
    fun getWeatherConfig(id: Int): Flow<ConfigProfile?> =
        weatherRepo.getConfigProfile(id)
    fun getRocketConfig(id: Int): Flow<RocketConfig?> =
        rocketRepo.getRocketConfig(id)
}