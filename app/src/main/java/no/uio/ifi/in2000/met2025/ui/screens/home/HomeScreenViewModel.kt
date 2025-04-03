package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val launchSiteDao: LaunchSiteDAO
) : ViewModel() {

    // UI state for the full screen (currently used for the launch sites list)
    sealed class HomeScreenUiState {
        object Loading : HomeScreenUiState()
        data class Success(
            val launchSites: List<LaunchSite>,
            // For full UI state we store the list.
            val apiKeyAvailable: Boolean,
            val isOnline: Boolean
        ) : HomeScreenUiState()
        data class Error(val message: String) : HomeScreenUiState()
    }

    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState: StateFlow<HomeScreenUiState> = _uiState

    // A separate state holding the current map coordinates.
    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    init {
        viewModelScope.launch {
            // At startup, try to load the "Last Visited" record.
            val lastVisited = launchSiteDao.getTempSite("Last Visited").first()
            if (lastVisited != null) {
                updateCoordinates(lastVisited.latitude, lastVisited.longitude)
            }
            loadLaunchSites()
        }
    }

    fun updateLastVisited(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val currentVisited = launchSiteDao.getTempSite("Last Visited").first()
                if (currentVisited == null) {
                    launchSiteDao.insertAll(
                        LaunchSite(latitude = lat, longitude = lon, name = "Last Visited")
                    )
                } else {
                    launchSiteDao.updateSites(currentVisited.copy(latitude = lat, longitude = lon))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateNewMarker(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val currentMarker = launchSiteDao.getTempSite("New Marker").first()
                if (currentMarker == null) {
                    launchSiteDao.insertAll(
                        LaunchSite(latitude = lat, longitude = lon, name = "New Marker")
                    )
                } else {
                    launchSiteDao.updateSites(currentMarker.copy(latitude = lat, longitude = lon))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    // When a marker is placed, update the local coordinates immediately,
    // then update (or insert) the "Last Visited" record in the DAO.
    fun onMarkerPlaced(lat: Double, lon: Double) {
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon)
        updateNewMarker(lat, lon)
        loadLaunchSites() // refresh UI state if needed
    }


    // Loads the full list of launch sites.
    fun loadLaunchSites() {
        viewModelScope.launch {
            try {
                val sites = launchSiteDao.getAll().first()
                // We don't update coordinates here; they come from _coordinates.
                _uiState.value = HomeScreenUiState.Success(
                    launchSites = sites,
                    apiKeyAvailable = true,
                    isOnline = true
                )
            } catch (e: Exception) {
                _uiState.value = HomeScreenUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Adds a launch site (permanent save) when the user confirms a dialog.
    fun addLaunchSite(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            try {
                launchSiteDao.insertAll(LaunchSite(latitude = lat, longitude = lon, name = name))
                loadLaunchSites()
            } catch (e: Exception) {
                _uiState.value = HomeScreenUiState.Error(e.message ?: "Failed to add launch site")
            }
        }
    }

    fun geocodeAddress(address: String): Pair<Double, Double>? {
        return if (address.contains("NYC", ignoreCase = true)) {
            Pair(40.7128, -74.0060)
        } else {
            null
        }
    }
}
