package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

@HiltViewModel
class ConfigEditViewModel @Inject constructor(
    private val configProfileRepository: ConfigProfileRepository
) : ViewModel() {

    fun saveConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileRepository.insertConfigProfile(config)
        }
    }

    fun updateConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileRepository.updateConfigProfile(config)
        }
    }

    fun getConfigProfile(configId: Int): Flow<ConfigProfile?> {
        return configProfileRepository.getConfigProfile(configId)
    }
}

