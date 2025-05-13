package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

import android.database.sqlite.SQLiteConstraintException
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
import no.uio.ifi.in2000.met2025.domain.IsobaricInterpolator
import no.uio.ifi.in2000.met2025.domain.RocketState
import no.uio.ifi.in2000.met2025.domain.TrajectoryCalculator
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import javax.inject.Inject
import java.time.Instant

// MapScreenViewModel.kt
@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val launchSiteRepository: LaunchSiteRepository,
    private val rocketConfigRepository: RocketConfigRepository,
    private val isobaricInterpolator: IsobaricInterpolator
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

    /** Call this when the user taps a config card */
    fun selectConfig(cfg: RocketConfig) {
        _selectedConfig.value = cfg
    }

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

    init {
        // 1) Load all launch sites
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
                    name = "Default Rocket Config",
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
        viewModelScope.launch {
            val tempSite = launchSiteRepository.getNewMarkerTempSite().firstOrNull()
            val newCoords = tempSite?.let { Pair(it.latitude, it.longitude) } ?: _coordinates.value
            updateCoordinates(newCoords.first, newCoords.second)
        }
        viewModelScope.launch {
            launchSiteRepository.getNewMarkerTempSite().collect { site ->
                _newMarker.value = site
            }
        }
        viewModelScope.launch {
            launchSiteRepository.getLastVisitedTempSite().collect { site ->
                _lastVisited.value = site
            }
        }
        viewModelScope.launch {
            if (launchSiteRepository.checkIfSiteExists("New Marker")) {
                _newMarkerStatus.value = true
            }
        }
        viewModelScope.launch {
            rocketConfigRepository.getDefaultRocketConfig().firstOrNull()?.let { /* ok */ }
                ?: rocketConfigRepository.insertRocketConfig(
                    mapToRocketConfig(
                        name = "Default Rocket Config",
                        values = getDefaultRocketParameterValues(),
                        isDefault = true
                    )
                )
        }
    }


    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    /** Allow null here: elevation pending until terrain query returns */
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

    /** Change edit/add APIs to accept nullable elevation too */
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

    /** Start the trajectory using the currently selected config */
    fun startTrajectory() {
        viewModelScope.launch {
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
                            timeOfLaunch = Instant.now()
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
            }
        }
    }

    fun onTrajectoryComplete() {
        isAnimating      = false
        isTrajectoryMode = false
    }

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
    /** Called when the user taps “⚙️ Rocket Configs” */
    fun showRocketConfigDialog() {
        // TODO: e.g. flip a StateFlow or send a UI‐event that your
        // dialog/popup code can observe and render.
    }

    /** Called when the user taps “📍 Show Current Lat/Lon” */
    fun showCurrentLatLon() {
        //val (lat, lon) = coordinates.value
        // TODO: e.g. push a Toast or UI‐event with "$lat, $lon"
    }

    /** Called when the user taps “🚀 Launch From Center” */
    fun launchHere() {
        // simply reuse your startTrajectory logic, or
        // if you need to update a “launch site” first do that
        //startTrajectory()
    }

    fun clearTrajectory() {
        _trajectoryPoints.value = emptyList()
        isAnimating = false
        isTrajectoryMode = false
    }

}


// Utility for bearing
fun calculateBearing(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)
    val y = Math.sin(Δλ) * Math.cos(φ2)
    val x = Math.cos(φ1) * Math.sin(φ2) -
            Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ)
    return Math.toDegrees(Math.atan2(y, x))
}