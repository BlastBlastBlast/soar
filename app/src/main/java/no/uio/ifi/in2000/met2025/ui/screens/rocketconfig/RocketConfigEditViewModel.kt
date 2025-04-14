// File: RocketConfigEditViewModel.kt
package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.data.models.RocketParameterValues
import no.uio.ifi.in2000.met2025.data.models.mapToRocketConfig
import javax.inject.Inject

@HiltViewModel
class RocketConfigEditViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {

    fun saveRocketConfig(rocketConfig: RocketConfig) {
        viewModelScope.launch {
            rocketConfigRepository.insertRocketConfig(rocketConfig)
        }
    }

    fun updateRocketConfig(rocketConfig: RocketConfig) {
        viewModelScope.launch {
            rocketConfigRepository.updateRocketConfig(rocketConfig)
        }
    }

    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?> {
        return rocketConfigRepository.getRocketConfig(rocketId)
    }
}