// File: RocketConfigEditViewModel.kt
package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

@HiltViewModel
class RocketConfigEditViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {

    fun saveRocketConfig(rocketParameters: RocketParameters) {
        viewModelScope.launch {
            rocketConfigRepository.insertRocketSpecs(rocketParameters)
        }
    }

    fun updateRocketConfig(rocketParameters: RocketParameters) {
        viewModelScope.launch {
            rocketConfigRepository.updateRocketSpecs(rocketParameters)
        }
    }

    fun getRocketConfig(rocketId: Int) = rocketConfigRepository.getRocketSpecs(rocketId)
}
