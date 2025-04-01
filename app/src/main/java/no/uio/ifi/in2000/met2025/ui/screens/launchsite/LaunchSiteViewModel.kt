package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import javax.inject.Inject

@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
) : ViewModel() {

    // Flow of all saved launch sites.
    val launchSites: Flow<List<LaunchSite>> = launchSiteDAO.getAll()

    // Flow of the temporary (last visited) launch site.
    val tempLaunchSite: Flow<LaunchSite?> = launchSiteDAO.getTempSite()

    // Update or create the temporary launch site with the given coordinates.
    fun updateTemporaryLaunchSite(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Retrieve the current temporary site (if any).
            val currentTempSite = tempLaunchSite.firstOrNull()
            if (currentTempSite == null) {
                // Insert a new temporary launch site.
                launchSiteDAO.insertAll(LaunchSite(
                    latitude = latitude,
                    longitude = longitude,
                    name = "Last Visited"
                ))
            } else {
                // Update the existing temporary launch site.
                launchSiteDAO.updateSites(
                    currentTempSite.copy(latitude = latitude, longitude = longitude)
                )
            }
        }
    }

    // Permanently add a launch site (e.g. when the user chooses to save it).
    fun addLaunchSite(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            launchSiteDAO.insertAll(LaunchSite(latitude = latitude, longitude = longitude, name = name))
        }
    }

    // Delete a launch site.
    fun deleteLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSiteDAO.delete(site)
        }
    }

    // Update an existing launch site.
    fun updateLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSiteDAO.updateSites(site)
        }
    }
}
