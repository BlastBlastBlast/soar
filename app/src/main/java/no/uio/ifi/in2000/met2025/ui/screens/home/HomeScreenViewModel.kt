package no.uio.ifi.in2000.met2025.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
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

    private val _launchSites = MutableStateFlow<List<LaunchSite>>(emptyList())
    val launchSites: StateFlow<List<LaunchSite>> = _launchSites

    private val _lastVisited = MutableStateFlow<LaunchSite?>(null)
    val lastVisited: StateFlow<LaunchSite?> = _lastVisited

    private val _newMarker = MutableStateFlow<LaunchSite?>(null)
    val newMarker: StateFlow<LaunchSite?> = _newMarker

    private val _newMarkerStatus = MutableStateFlow(false)
    val newMarkerStatus: StateFlow<Boolean> = _newMarkerStatus

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
                _launchSites.value = sites
                _uiState.value = HomeScreenUiState.Success(
                    launchSites = sites,
                    apiKeyAvailable = true,
                    isOnline = true
                )
            }
        }
        viewModelScope.launch {
            val tempSite = launchSitesRepository.getNewMarkerTempSite().firstOrNull()
            val newCoords = tempSite?.let { Pair(it.latitude, it.longitude) } ?: _coordinates.value
            updateCoordinates(newCoords.first, newCoords.second)
        }
        viewModelScope.launch {
            launchSitesRepository.getNewMarkerTempSite().collect { site ->
                _newMarker.value = site
            }
        }
        viewModelScope.launch {
            launchSitesRepository.getLastVisitedTempSite().collect { site ->
                _lastVisited.value = site
            }
        }
        viewModelScope.launch {
            if (launchSitesRepository.checkIfSiteExists("New Marker")) {
                _newMarkerStatus.value = true
            }
        }
    }

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    fun updateLastVisited(lat: Double, lon: Double, elevation: Double) {
        viewModelScope.launch {
            try {
                val exists = launchSitesRepository.checkIfSiteExists("Last Visited")
                if (exists && lastVisited.value != null) {
                    launchSitesRepository.update(
                        LaunchSite(
                            uid       = lastVisited.value!!.uid,
                            latitude  = lat,
                            longitude = lon,
                            name      = "Last Visited",
                            elevation = elevation
                        )
                    )
                } else {
                    launchSitesRepository.insert(
                        LaunchSite(
                            latitude  = lat,
                            longitude = lon,
                            name      = "Last Visited",
                            elevation = elevation
                        )
                    )
                }
            } catch (e: SQLiteConstraintException) {
                e.printStackTrace()
                _uiState.value = HomeScreenUiState.Error(
                    "${e.message ?: "Unknown error"} for Last Visited"
                )
            }
        }
    }

    fun updateNewMarker(lat: Double, lon: Double, elevation: Double) {
        viewModelScope.launch {
            try {
                val exists = launchSitesRepository.checkIfSiteExists("New Marker")
                if (exists && newMarker.value != null) {
                    launchSitesRepository.update(
                        LaunchSite(
                            uid       = newMarker.value!!.uid,
                            latitude  = lat,
                            longitude = lon,
                            name      = "New Marker",
                            elevation = elevation
                        )
                    )
                } else {
                    launchSitesRepository.insert(
                        LaunchSite(
                            latitude  = lat,
                            longitude = lon,
                            name      = "New Marker",
                            elevation = elevation
                        )
                    )
                }
            } catch (e: SQLiteConstraintException) {
                e.printStackTrace()
                _uiState.value = HomeScreenUiState.Error(
                    "${e.message ?: "Unknown error"} for New Marker"
                )
            }
        }
    }

    fun editLaunchSite(siteId: Int, lat: Double, lon: Double, elevation: Double, name: String) {
        viewModelScope.launch {
            val exists = launchSitesRepository.checkIfSiteExists(name)
            if (exists) {
                _updateStatus.value = UpdateStatus.Error("Launch site with this name already exists")
            } else {
                launchSitesRepository.update(
                    LaunchSite(
                        uid       = siteId,
                        latitude  = lat,
                        longitude = lon,
                        name      = name,
                        elevation = elevation
                    )
                )
                _updateStatus.value = UpdateStatus.Success
            }
        }
    }

    fun onMarkerPlaced(lat: Double, lon: Double, elevation: Double) {
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon, elevation)
        updateNewMarker(lat, lon, elevation)
        _newMarkerStatus.value = true
    }

    fun setNewMarkerStatusFalse() {
        _newMarkerStatus.value = false
    }

    fun addLaunchSite(lat: Double, lon: Double, elevation: Double, name: String) {
        viewModelScope.launch {
            try {
                launchSitesRepository.insert(
                    LaunchSite(
                        latitude  = lat,
                        longitude = lon,
                        elevation = elevation,
                        name      = name
                    )
                )
                _updateStatus.value = UpdateStatus.Success
            } catch (e: SQLiteConstraintException) {
                _updateStatus.value = UpdateStatus.Error("Launch site with this name already exists")
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

    fun updateSiteElevation(siteId: Int, elevation: Double) {
        viewModelScope.launch {
            launchSitesRepository.updateElevation(siteId, elevation)
        }
    }
}