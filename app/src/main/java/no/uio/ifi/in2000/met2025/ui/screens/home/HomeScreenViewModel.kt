package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val launchSitesRepository: LaunchSitesRepository
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
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    // Coordinates used for the map’s center
    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates.asStateFlow()

    init {
        // Collect launch sites and update UI state.
        viewModelScope.launch {
            launchSitesRepository.getAll().collect { sites ->
                // Get "Last Visited" temporary marker, if available.
                val tempSite = launchSitesRepository.getTempSite("Last Visited").firstOrNull()
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
                val currentVisited = launchSitesRepository.getTempSite("Last Visited").firstOrNull()
                if (currentVisited == null) {
                    launchSitesRepository.insertAll(
                        LaunchSite(latitude = lat, longitude = lon, name = "Last Visited")
                    )
                } else {
                    launchSitesRepository.updateSites(currentVisited.copy(latitude = lat, longitude = lon))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateNewMarker(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val currentMarker = launchSitesRepository.getNewMarkerTempSite("New Marker").firstOrNull()
                if (currentMarker == null) {
                    launchSitesRepository.insertAll(
                        LaunchSite(latitude = lat, longitude = lon, name = "New Marker")
                    )
                } else {
                    launchSitesRepository.updateSites(currentMarker.copy(latitude = lat, longitude = lon))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMarkerPlaced(lat: Double, lon: Double) {
        // Update the current coordinates as well as both temporary markers.
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon)
        updateNewMarker(lat, lon)
    }

    fun addLaunchSite(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            try {
                launchSitesRepository.insertAll(
                    LaunchSite(latitude = lat, longitude = lon, name = name)
                )
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