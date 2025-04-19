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

// HomeScreenViewModel.kt
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

    sealed class UpdateStatus {
        object Idle : UpdateStatus()
        object Success : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    init {
        viewModelScope.launch {
            launchSitesRepository.getAll().collect { sites ->
                // Use the received list of LaunchSite objects directly.
                _launchSites.value = sites

                // Optionally update coordinates based on a temporary "Last Visited" record.
                val tempSite = launchSitesRepository.getLastVisitedTempSite().firstOrNull()
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
                val exists = launchSitesRepository.checkIfSiteExists("Last Visited")
                if (exists) {
                    launchSitesRepository.update(
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "Last Visited"
                        )
                    )
                } else {
                    launchSitesRepository.insert(
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "Last Visited"
                        )
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
                val exists = launchSitesRepository.checkIfSiteExists("New Marker")
                if (exists) {
                    launchSitesRepository.update(
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "New Marker"
                        )
                    )
                } else {
                    launchSitesRepository.insert(
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            name = "New Marker"
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun editLaunchSite(siteId: Int, lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            // Create an updated LaunchSite instance.
            // Assuming your LaunchSite data class has properties: uid, name, latitude, longitude.
            val exists = launchSitesRepository.checkIfSiteExists(name)
            if (exists) {
                _updateStatus.value = UpdateStatus.Error("Launch site with this name already exists")
            } else {
                val updatedSite = LaunchSite(
                    uid = siteId,
                    name = name,
                    latitude = lat,
                    longitude = lon
                )
                // Use your repository's update function.
                launchSitesRepository.update(updatedSite)
                _updateStatus.value = UpdateStatus.Success
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
                launchSitesRepository.insert(
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

    fun setUpdateStatusIdle() {
        _updateStatus.value = UpdateStatus.Idle
    }

    fun geocodeAddress(address: String): Pair<Double, Double>? {
        return if (address.contains("NYC", ignoreCase = true)) {
            Pair(40.7128, -74.0060)
        } else {
            null
        }
    }
}