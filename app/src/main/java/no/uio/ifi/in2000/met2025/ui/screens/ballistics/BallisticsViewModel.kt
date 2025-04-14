package no.uio.ifi.in2000.met2025.ui.screens.ballistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.data.models.getDefaultRocketParameterValues
import no.uio.ifi.in2000.met2025.data.models.mapToRocketConfig
import javax.inject.Inject

@HiltViewModel
class BallisticsViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {

    // Expose the default rocket configuration as a Flow for UI observation.
    val defaultRocketConfig = rocketConfigRepository.getDefaultRocketParameters()

    init {
        viewModelScope.launch {
            // Check if a default config exists already.
            val currentDefault = rocketConfigRepository.getDefaultRocketParameters().first()
            if (currentDefault == null) {
                // No default config is present; get the default model values.
                val defaultValues = getDefaultRocketParameterValues()
                // Map the model values to the RocketConfig database entity.
                val newDefault = mapToRocketConfig(
                    name = "Default Rocket Config",
                    values = defaultValues,
                    isDefault = true
                )
                rocketConfigRepository.insertRocketParameters(newDefault)
            }
        }
    }
}