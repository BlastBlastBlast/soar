package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val launchSiteDao: LaunchSiteDAO
) : ViewModel() {

    sealed class HomeScreenUiState {
        object Loading : HomeScreenUiState()
        data class Success(
            val launchSites: List<LaunchSite>,
            val apiKeyAvailable: Boolean,
            val isOnline: Boolean
        ) : HomeScreenUiState()
        data class Error(val message: String) : HomeScreenUiState()
    }

    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState: StateFlow<HomeScreenUiState> = _uiState

    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    init {
        // Continuously collect the launch sites list so that any update is emitted.
        viewModelScope.launch {
            launchSiteDao.getAll().collect { sites ->
                // Get the "Last Visited" record continuously.
                val tempSite = launchSiteDao.getTempSite("Last Visited").firstOrNull()
                val newCoords = tempSite?.let { Pair(it.latitude, it.longitude) } ?: _coordinates.value
                updateCoordinates(newCoords.first, newCoords.second)
                _uiState.value = HomeScreenUiState.Success(
                    launchSites = sites,
                    apiKeyAvailable = true,
                    isOnline = true
                )
            }
        }
    }

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    fun updateLastVisited(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val currentVisited = launchSiteDao.getTempSite("Last Visited").firstOrNull()
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
                val currentMarker = launchSiteDao.getTempSite("New Marker").firstOrNull()
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

    fun onMarkerPlaced(lat: Double, lon: Double) {
        // When placing a marker, update both temporary records.
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon)
        updateNewMarker(lat, lon)
        // No manual loadLaunchSites() call is necessary now,
        // as the continuous Flow collection will update the UI state.
    }

    fun addLaunchSite(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            try {
                launchSiteDao.insertAll(LaunchSite(latitude = lat, longitude = lon, name = name))
                // The continuous flow in init will pick up this change.
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
