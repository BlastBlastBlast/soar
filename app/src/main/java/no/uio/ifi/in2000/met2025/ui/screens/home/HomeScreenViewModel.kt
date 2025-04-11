package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import javax.inject.Inject

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
    val uiState: StateFlow<HomeScreenUiState> = _uiState

    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    // StateFlow that holds the list of UI launch sites.
    private val _launchSites = MutableStateFlow<List<LaunchSite>>(emptyList())
    val launchSites: StateFlow<List<LaunchSite>> = _launchSites

    init {
        viewModelScope.launch {
            launchSitesRepository.getAll().collect { sites ->
                // Use the received list of LaunchSite objects directly.
                _launchSites.value = sites

                // Optionally update coordinates based on a temporary "Last Visited" record.
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
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "Last Visited"
                        )
                    )
                } else {
                    launchSitesRepository.updateSites(
                        currentVisited.copy(latitude = lat, longitude = lon)
                    )
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
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "New Marker"
                        )
                    )
                } else {
                    launchSitesRepository.updateSites(
                        currentMarker.copy(latitude = lat, longitude = lon)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onMarkerPlaced(lat: Double, lon: Double) {
        // When a marker is placed, update both temporary records via the repository.
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon)
        updateNewMarker(lat, lon)
    }

    fun addLaunchSite(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            try {
                launchSitesRepository.insertAll(
                    LaunchSite(
                        latitude = lat,
                        longitude = lon,
                        name = name
                    )
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