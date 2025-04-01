package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import javax.inject.Inject

@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
) : ViewModel() {

    // Observe all saved launch sites.
    val launchSites: Flow<List<LaunchSite>> = launchSiteDAO.getAll()

    // Add a new launch site.
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
