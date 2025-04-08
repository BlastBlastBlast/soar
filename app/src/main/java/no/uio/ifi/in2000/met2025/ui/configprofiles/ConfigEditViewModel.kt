package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO

@HiltViewModel
class ConfigEditViewModel @Inject constructor(
    private val configProfileDao: ConfigProfileDAO
) : ViewModel() {
    fun saveConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileDao.insertConfigProfile(config)
        }
    }
    fun updateConfig(config: ConfigProfile) {
        viewModelScope.launch {
            configProfileDao.updateConfigProfile(config)
        }
    }
}
