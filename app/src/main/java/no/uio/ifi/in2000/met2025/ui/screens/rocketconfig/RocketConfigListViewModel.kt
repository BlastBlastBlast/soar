package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

// File: RocketConfigListViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

@HiltViewModel
class RocketConfigListViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {
    val rocketList = rocketConfigRepository.getAllRocketParameters()

    fun deleteRocketConfig(rocketParameters: RocketParameters) {
        viewModelScope.launch {
            rocketConfigRepository.deleteRocketParameters(rocketParameters)
        }
    }
}
