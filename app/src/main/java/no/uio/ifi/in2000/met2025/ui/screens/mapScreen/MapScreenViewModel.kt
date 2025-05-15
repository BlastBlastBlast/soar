/*
 * ViewModel for the Map screen:
 *  - Loads and exposes launch sites, coordinates, and rocket configs
 *  - Handles insertion/update of “Last Visited” and “New Marker” sites
 *  - Computes and publishes rocket trajectory points via physics simulation
 *
 * Special notes:
 *  - Uses Flow + StateFlow to stream UI state
 *  - Persists data with LaunchSiteRepository and RocketConfigRepository
 */

package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSiteRepository
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.data.models.getDefaultRocketParameterValues
import no.uio.ifi.in2000.met2025.data.models.mapToRocketConfig
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.IsobaricInterpolator
import no.uio.ifi.in2000.met2025.domain.RocketState
import no.uio.ifi.in2000.met2025.domain.TrajectoryCalculator
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import javax.inject.Inject
import java.time.Instant

/**
 * Provides all data and actions for MapScreen:
 *  - Exposes UI state (Loading/Success/Error)
 *  - Streams coordinate updates and saved sites
 *  - Orchestrates trajectory simulation using TrajectoryCalculator
 */
@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val launchSiteRepository: LaunchSiteRepository,
    private val rocketConfigRepository: RocketConfigRepository,
    private val isobaricInterpolator: IsobaricInterpolator,
    private val isobaricRepository: IsobaricRepository
) : ViewModel() {

    sealed class MapScreenUiState {
        object Loading : MapScreenUiState()
        data class Success(
            val launchSites: List<LaunchSite>,
            val apiKeyAvailable: Boolean,
            val isOnline: Boolean
        ) : MapScreenUiState()

        data class Error(val message: String) : MapScreenUiState()
    }

    private val _rocketConfigList = MutableStateFlow<List<RocketConfig>>(emptyList())
    val rocketConfigList: StateFlow<List<RocketConfig>> = _rocketConfigList

    private val _selectedConfig = MutableStateFlow<RocketConfig?>(null)
    val selectedConfig: StateFlow<RocketConfig?> = _selectedConfig

    /** Called when the user taps a config card */
    fun selectConfig(cfg: RocketConfig) { _selectedConfig.value = cfg }

    private val _trajectoryPoints = MutableStateFlow<List<Triple<RealVector, Double, RocketState>>>(emptyList())
    val trajectoryPoints: StateFlow<List<Triple<RealVector, Double, RocketState>>> = _trajectoryPoints

    var isAnimating      by mutableStateOf(false)
    var isTrajectoryMode by mutableStateOf(false)

    private val _uiState = MutableStateFlow<MapScreenUiState>(MapScreenUiState.Loading)
    val uiState: StateFlow<MapScreenUiState> = _uiState

    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    private val _launchSites = MutableStateFlow<List<LaunchSite>>(emptyList())
    val launchSites: StateFlow<List<LaunchSite>> = _launchSites

    private val _lastVisited = MutableStateFlow<LaunchSite?>(null)
    val lastVisited: StateFlow<LaunchSite?> = _lastVisited

    private val _currentSite: StateFlow<LaunchSite?> =
        launchSiteRepository.getActiveSite().stateIn(
            viewModelScope, SharingStarted.Eagerly, null
        )
    val currentSite: StateFlow<LaunchSite?> = _currentSite

    private val _isTrajectoryCalculating = MutableStateFlow(false)
    val isTrajectoryCalculating: StateFlow<Boolean> = _isTrajectoryCalculating

    private val _newMarker = MutableStateFlow<LaunchSite?>(null)
    val newMarker: StateFlow<LaunchSite?> = _newMarker

    private val _newMarkerStatus = MutableStateFlow(false)
    val newMarkerStatus: StateFlow<Boolean> = _newMarkerStatus

    private val _launchSiteName = MutableStateFlow("")
    val launchSiteName: StateFlow<String> = _launchSiteName

    sealed class UpdateStatus {
        object Idle : UpdateStatus()
        object Success : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    private val _latestAvailableGrib = MutableStateFlow<Instant?>(null)
    val latestAvailableGrib: StateFlow<Instant?> = _latestAvailableGrib

    /**
     * Startup tasks:
     *  1) Load saved launch sites
     *  2) Ensure a default rocket config exists
     *  3) Restore any temporary “New Marker” or “Last Visited” site
     */

    init {
        // 1) Load saved launch sites
        viewModelScope.launch {
            launchSiteRepository.getAll().collect { sites ->
                _launchSites.value = sites
                _uiState.value = MapScreenUiState.Success(
                    launchSites = sites,
                    apiKeyAvailable = true,
                    isOnline = true
                )
            }
        }
        // 2) Ensure a default rocket config exists
        viewModelScope.launch {
            val default = rocketConfigRepository.getDefaultRocketConfig().firstOrNull()
            if (default == null) {
                val defaultCfg = mapToRocketConfig(
                    name = "Default",
                    values = getDefaultRocketParameterValues(),
                    isDefault = true
                )
                rocketConfigRepository.insertRocketConfig(defaultCfg)
                _selectedConfig.value = defaultCfg
            } else {
                _selectedConfig.value = default
            }
            rocketConfigRepository.getAllRocketConfigs().collect { configs ->
                _rocketConfigList.value = configs
            }
        }
        // 3) Restore any temporary “New Marker” or “Last Visited” site
        viewModelScope.launch {
            val tempSite = launchSiteRepository.getNewMarkerTempSite().firstOrNull()
            val newCoords = tempSite?.let { Pair(it.latitude, it.longitude) } ?: _coordinates.value
            updateCoordinates(newCoords.first, newCoords.second)
        }
        // Get the new marker site
        viewModelScope.launch {
            launchSiteRepository.getNewMarkerTempSite().collect { site ->
                _newMarker.value = site
            }
        }
        // Get the last visited site
        viewModelScope.launch {
            launchSiteRepository.getLastVisitedTempSite().collect { site ->
                _lastVisited.value = site
            }
        }
        // Check if the new marker site exists
        viewModelScope.launch {
            if (launchSiteRepository.checkIfSiteExists("New Marker")) {
                _newMarkerStatus.value = true
            }
        }
    }


    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    /**
     * Inserts or updates the “Last Visited” site with nullable elevation.
     * Catches constraint errors and reports via uiState.
     */
    fun updateLastVisited(lat: Double, lon: Double, elevation: Double?) {
        viewModelScope.launch {
            try {
                val exists = launchSiteRepository.checkIfSiteExists("Last Visited")
                val site = LaunchSite(
                    uid = lastVisited.value?.uid ?: 0,
                    latitude = lat,
                    longitude = lon,
                    name = "Last Visited",
                    elevation = elevation   // now nullable column
                )
                if (exists && lastVisited.value != null) {
                    launchSiteRepository.update(site)
                } else {
                    launchSiteRepository.insert(site)
                }
            } catch (e: SQLiteConstraintException) {
                _uiState.value = MapScreenUiState.Error(
                    "${e.message ?: "Unknown error"} for Last Visited"
                )
            }
        }
    }

    fun reloadScreen() {
        _uiState.value = MapScreenUiState.Success(
            launchSites = launchSites.value,
            apiKeyAvailable = true,
            isOnline = true
        )
    }
    /**
     * Inserts or updates the “New Marker” site with nullable elevation.
     * Catches constraint errors and reports via uiState.
     */
    fun updateNewMarker(lat: Double, lon: Double, elevation: Double?) {
        viewModelScope.launch {
            try {
                val exists = launchSiteRepository.checkIfSiteExists("New Marker")
                val site = LaunchSite(
                    uid = newMarker.value?.uid ?: 0,
                    latitude = lat,
                    longitude = lon,
                    name = "New Marker",
                    elevation = elevation
                )
                if (exists && newMarker.value != null) {
                    launchSiteRepository.update(site)
                } else {
                    launchSiteRepository.insert(site)
                }
            } catch (e: Exception) {
                _uiState.value =
                    MapScreenUiState.Error("Error saving marker elevation: ${e.message}")
            }
        }
    }

    /**
     * Validates unique name and updates an existing launch site.
     * Updates _updateStatus to Success or Error.
     */
    fun editLaunchSite(siteId: Int, lat: Double, lon: Double, elevation: Double?, name: String) {
        viewModelScope.launch {
            val nameSite = launchSiteRepository.getSiteByName(name)
            val exists = nameSite != null
            val sameSite = nameSite?.uid == siteId
            if (exists && !sameSite) {
                    _updateStatus.value =
                        UpdateStatus.Error("Launch site with this name already exists")
            } else {
                launchSiteRepository.update(
                    LaunchSite(
                        uid = siteId,
                        latitude = lat,
                        longitude = lon,
                        name = name,
                        elevation = elevation
                    )
                )
                _updateStatus.value = UpdateStatus.Success
            }
        }
    }

    /**
     * Runs the rocket trajectory simulation:
     *  1) Retrieves selected config and last-visited coords
     *  2) Builds initial vector with elevation
     *  3) Calls TrajectoryCalculator.calculateTrajectory(...)
     *  4) Publishes points and triggers camera animation
     */
    fun startTrajectory(timeOfLaunch: Instant) {
        viewModelScope.launch {
            _isTrajectoryCalculating.value = true
            try {
                // 1) Grab the current default/selected config
                val cfg = selectedConfig.value ?: return@launch

                // 2) Pull the “last visited” lat/lon directly from the repo:
                val (lat, lon) = launchSiteRepository
                    .getCurrentCoordinates()    // Flow<Pair<Double,Double>>
                    .first()                    // suspend until we get the latest

                // 2) Build the initial position from your center coords + elevation
                val elev = launchSiteRepository.getLastVisitedElevation()
                val initial = ArrayRealVector(doubleArrayOf(lat, lon, elev))
                val traj: List<Triple<RealVector, Double, RocketState>> =
                    TrajectoryCalculator(isobaricInterpolator)
                        // 3) Run the physics‐based sim
                        .calculateTrajectory(
                            initialPosition = initial,
                            launchAzimuthInDegrees = cfg.launchAzimuth,
                            launchPitchInDegrees = cfg.launchPitch,
                            launchRailLength = cfg.launchRailLength,
                            wetMass = cfg.wetMass,
                            dryMass = cfg.dryMass,
                            burnTime = cfg.burnTime,
                            thrust = cfg.thrust,
                            stepSize = cfg.stepSize,
                            crossSectionalArea = cfg.crossSectionalArea,
                            dragCoefficient = cfg.dragCoefficient,
                            parachuteCrossSectionalArea = cfg.parachuteCrossSectionalArea,
                            parachuteDragCoefficient = cfg.parachuteDragCoefficient,
                            timeOfLaunch = timeOfLaunch
                        ).getOrThrow()

                // 4) Publish the points & kick off the camera animation
                _trajectoryPoints.value = traj
                isAnimating = true
                isTrajectoryMode = true
            } catch (e: Exception) {
                _uiState.value = MapScreenUiState.Error(
                    "Something went wrong with the launch simulation. " +
                            "The calculations use weather data fetched in real time, " +
                            "so please check your internet connection and try again."
                )
            } finally {
                _isTrajectoryCalculating.value = false
            }
        }
    }

    fun onTrajectoryComplete() {
        isAnimating      = false
        isTrajectoryMode = false
    }

    /**
     * Central handler when user taps or long-presses map:
     *  - Updates center coords, “Last Visited” and “New Marker” entries
     */
    fun onMarkerPlaced(lat: Double, lon: Double, elevation: Double?) {
        updateCoordinates(lat, lon)
        updateLastVisited(lat, lon, elevation)
        updateNewMarker(lat, lon, elevation)
        _newMarkerStatus.value = true
    }

    fun setNewMarkerStatusFalse() {
        _newMarkerStatus.value = false
    }

    fun addLaunchSite(lat: Double, lon: Double, elevation: Double?, name: String) {
        viewModelScope.launch {
            try {
                if (name == "New Marker") {
                    _updateStatus.value = UpdateStatus.Error("New Marker is not a valid name")
                } else {
                    launchSiteRepository.insert(
                        LaunchSite(
                            latitude = lat,
                            longitude = lon,
                            elevation = elevation,
                            name = name
                        )
                    )
                    _updateStatus.value = UpdateStatus.Success
                }
            } catch (e: SQLiteConstraintException) {
                _updateStatus.value =
                    UpdateStatus.Error("Launch site with this name already exists")
            }
        }
    }

    fun setUpdateStatusIdle() {
        _updateStatus.value = UpdateStatus.Idle
    }

    fun updateSiteElevation(siteId: Int, elevation: Double) {
        viewModelScope.launch {
            launchSiteRepository.updateElevation(siteId, elevation)
        }
    }

    fun updateLaunchSiteName(name: String) {
        _launchSiteName.value = name
    }

    fun clearTrajectory() {
        _trajectoryPoints.value = emptyList()
        isAnimating = false
        isTrajectoryMode = false
    }

    suspend fun updateLatestAvailableGrib() {
        _latestAvailableGrib.value = isobaricRepository.getLatestAvailableGrib()
    }

}

fun calculateBearing(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)
    val y = Math.sin(Δλ) * Math.cos(φ2)
    val x = Math.cos(φ1) * Math.sin(φ2) -
            Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ)
    return Math.toDegrees(Math.atan2(y, x))
}