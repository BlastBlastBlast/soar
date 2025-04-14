// File: RocketConfigEditViewModel.kt
package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.data.models.RocketConfig
import javax.inject.Inject

@HiltViewModel
class RocketConfigEditViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {

    fun saveRocketConfig(rocketConfig: RocketConfig) {
        viewModelScope.launch {
            rocketConfigRepository.insertRocketParameters(rocketConfig)
        }
    }

    fun updateRocketConfig(rocketConfig: RocketConfig) {
        viewModelScope.launch {
            rocketConfigRepository.updateRocketParameters(rocketConfig)
        }
    }

    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?> {
        return rocketConfigRepository.getRocketParameters(rocketId)
    }
}