
package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import javax.inject.Inject

@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSitesRepository: LaunchSitesRepository
) : ViewModel() {

    // Flow of all saved launch sites.
    val launchSites: Flow<List<LaunchSite>> = launchSitesRepository.getAll()

    // Flow for "Last Visited" temporary site.
    val tempLaunchSite: Flow<LaunchSite?> = launchSitesRepository.getLastVisitedTempSite()

    // Flow for "New Marker" temporary site.
    val newMarkerTempSite: Flow<LaunchSite?> = launchSitesRepository.getNewMarkerTempSite()

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
            launchSitesRepository.getAllLaunchSiteNames().collect { names ->
                _launchSiteNames.value = names
            }
        }
    }

    // Update "Last Visited" temporary site.
    fun updateTemporaryLaunchSite(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val currentTempSite = tempLaunchSite.firstOrNull()
            if (currentTempSite == null) {
                launchSitesRepository.insert(
                    LaunchSite(latitude = latitude, longitude = longitude, name = "Last Visited")
                )
            } else {
                launchSitesRepository.update(
                    currentTempSite.copy(latitude = latitude, longitude = longitude)
                )
            }
        }
    }

    // Update "New Marker" temporary site.
    fun updateNewMarkerSite(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val currentNewMarker = newMarkerTempSite.firstOrNull()
            if (currentNewMarker == null) {
                launchSitesRepository.insert(
                    LaunchSite(latitude = latitude, longitude = longitude, name = "New Marker")
                )
            } else {
                launchSitesRepository.update(
                    currentNewMarker.copy(latitude = latitude, longitude = longitude)
                )
            }
        }
    }

    // Permanently add a launch site.
    fun addLaunchSite(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            launchSitesRepository.insert(LaunchSite(latitude = latitude, longitude = longitude, name = name))
        }
    }

    fun deleteLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSitesRepository.deleteSite(site)
        }
    }

    fun updateLaunchSite(launchSite: LaunchSite, name: String) {
        viewModelScope.launch {
            try {
                // Create an updated LaunchSite instance.
                // Assuming your LaunchSite data class has properties: uid, name, latitude, longitude.
                val exists = launchSitesRepository.checkIfSiteExists(launchSite.name)
                if (exists && launchSite.name != name) {
                    _updateStatus.value =
                        UpdateStatus.Error("Launch site with this name already exists")
                } else {
                    // Use your repository's update function.
                    if (launchSite.name != name) {
                        launchSitesRepository.update(launchSite)
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
