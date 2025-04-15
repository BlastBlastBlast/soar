
package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import javax.inject.Inject

@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSitesRepository: LaunchSitesRepository
) : ViewModel() {

    // Flow of all saved launch sites.
    val launchSites: Flow<List<LaunchSite>> = launchSitesRepository.getAll()

    // Flow for "Last Visited" temporary site.
    val tempLaunchSite: Flow<LaunchSite?> = launchSitesRepository.getTempSite()

    // Flow for "New Marker" temporary site.
    val newMarkerTempSite: Flow<LaunchSite?> = launchSitesRepository.getNewMarkerTempSite()

    // Update "Last Visited" temporary site.
    fun updateTemporaryLaunchSite(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val currentTempSite = tempLaunchSite.firstOrNull()
            if (currentTempSite == null) {
                launchSitesRepository.insertAll(
                    LaunchSite(latitude = latitude, longitude = longitude, name = "Last Visited")
                )
            } else {
                launchSitesRepository.updateSites(
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
                launchSitesRepository.insertAll(
                    LaunchSite(latitude = latitude, longitude = longitude, name = "New Marker")
                )
            } else {
                launchSitesRepository.updateSites(
                    currentNewMarker.copy(latitude = latitude, longitude = longitude)
                )
            }
        }
    }

    // Permanently add a launch site.
    fun addLaunchSite(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            launchSitesRepository.insertAll(LaunchSite(latitude = latitude, longitude = longitude, name = name))
        }
    }

    fun deleteLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSitesRepository.deleteSite(site)
        }
    }

    fun updateLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSitesRepository.updateSites(site)
        }
    }
}
