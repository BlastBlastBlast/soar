package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

// File: RocketConfigListViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import javax.inject.Inject

@HiltViewModel
class RocketConfigListViewModel @Inject constructor(
    private val rocketConfigRepository: RocketConfigRepository
) : ViewModel() {
    val rocketList = rocketConfigRepository.getAllRocketConfigs()

    fun deleteRocketConfig(rocketConfig: RocketConfig) {
        viewModelScope.launch {
            rocketConfigRepository.deleteRocketConfig(rocketConfig)
        }
    }
}