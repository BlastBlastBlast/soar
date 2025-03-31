package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import javax.inject.Inject


//TODO: implementere UI-states
@HiltViewModel
class LaunchSiteViewModel @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
) : ViewModel() {

    // Henter alle lagrede koordinater
    val launchSites: Flow<List<LaunchSite>> = launchSiteDAO.getAll()

    // Legg til et nytt oppskytningspunkt
    fun addLaunchSite(latitude: Double, longitude: Double, name: String) {
        viewModelScope.launch {
            launchSiteDAO.insertAll(LaunchSite(latitude = latitude, longitude = longitude, name = name))
        }
    }

    // Slett et oppskytningspunkt
    fun deleteLaunchSite(site: LaunchSite) {
        viewModelScope.launch {
            launchSiteDAO.delete(site)
        }
    }
}