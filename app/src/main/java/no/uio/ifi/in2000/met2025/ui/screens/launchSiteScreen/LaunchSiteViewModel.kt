
package no.uio.ifi.in2000.met2025.ui.screens.launchSiteScreen

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSiteRepository
import javax.inject.Inject

@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSiteRepository: LaunchSiteRepository
) : ViewModel() {

    // Flow of all saved launch sites.
    val launchSites: Flow<List<LaunchSite>> = launchSiteRepository.getAll()

    sealed class UpdateStatus {
        object Idle : UpdateStatus()
        data class Success(val siteUid: Int) : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    private val _launchSiteNames = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            launchSiteRepository.getAllLaunchSiteNames().collect { names ->
                _launchSiteNames.value = names
            }
        }
    }

    fun deleteLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSiteRepository.deleteSite(site)
        }
    }

    fun updateLaunchSite(launchSite: LaunchSite, name: String) {
        viewModelScope.launch {
            try {
                // Create an updated LaunchSite instance.
                // Assuming your LaunchSite data class has properties: uid, name, latitude, longitude.
                val exists = launchSiteRepository.checkIfSiteExists(launchSite.name)
                if (exists && launchSite.name != name) {
                    _updateStatus.value =
                        UpdateStatus.Error("Launch site with this name already exists")
                } else {
                    // Use your repository's update function.
                    if (launchSite.name != name) {
                        launchSiteRepository.update(launchSite)
                        _updateStatus.value = UpdateStatus.Success(launchSite.uid)
                    }
                }
            } catch (e: SQLiteConstraintException) {
                _updateStatus.value = UpdateStatus.Error("SQL Error: ${e.message}")
            }
        }
    }

    fun setUpdateStatusToIdle() {
        _updateStatus.value = UpdateStatus.Idle
    }

    fun checkNameAvailability(name: String) {
        viewModelScope.launch {
            for (existingName in _launchSiteNames.value) {
                if (existingName == name) {
                    _updateStatus.value = UpdateStatus.Error("Launch site with this name already exists")
                    return@launch
                }
                _updateStatus.value = UpdateStatus.Idle
            }
        }
    }
}
