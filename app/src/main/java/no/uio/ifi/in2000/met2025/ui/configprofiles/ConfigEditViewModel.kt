package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

@HiltViewModel
class ConfigEditViewModel @Inject constructor(
    private val configProfileRepository: ConfigProfileRepository
) : ViewModel() {

    sealed class UpdateStatus{
        object Success: UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
        object Idle: UpdateStatus()
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    private val _configNames = MutableStateFlow<List<String>>(emptyList())
    val configNames: StateFlow<List<String>> = _configNames

    init {
        viewModelScope.launch {
            configProfileRepository.getAllConfigProfileNames().collect { names ->
                _configNames.value = names
            }
        }
    }

    fun setUpdateStatusToIdle() {
        _updateStatus.value = UpdateStatus.Idle
    }

    fun saveConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileRepository.insertConfigProfile(config)
        }
    }

    fun updateConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileRepository.updateConfigProfile(config)
            _updateStatus.value = UpdateStatus.Success
        }
    }

    fun checkNameAvailability(name: String) {
        viewModelScope.launch {
            for (existingName in configNames.value) {
                if (existingName == name) {
                    _updateStatus.value = UpdateStatus.Error("Config with this name already exists")
                    return@launch
                }
            }
            _updateStatus.value = UpdateStatus.Idle
        }
    }

    fun getConfigProfile(configId: Int): Flow<ConfigProfile?> {
        return configProfileRepository.getConfigProfile(configId)
    }

}

