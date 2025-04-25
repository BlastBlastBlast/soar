package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

@HiltViewModel
class ConfigListViewModel @Inject constructor(
    private val configProfileRepository: ConfigProfileRepository
) : ViewModel() {
    val configList = configProfileRepository.getAllConfigProfiles()

    fun deleteConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileRepository.deleteConfigProfile(config)
        }
    }
}
