package no.uio.ifi.in2000.met2025.ui.screens.home

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.domain.IsobaricInterpolator
import no.uio.ifi.in2000.met2025.domain.TrajectoryCalculator
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import javax.inject.Inject
import kotlin.math.sin

// HomeScreenViewModel.kt
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val launchSitesRepository: LaunchSitesRepository,
    private val rocketConfigRepository: RocketConfigRepository,
    private val isobaricInterpolator: IsobaricInterpolator
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

    private val _trajectoryPoints = MutableStateFlow<List<Pair<RealVector, Double>>>(emptyList())
    val trajectoryPoints: StateFlow<List<Pair<RealVector, Double>>> = _trajectoryPoints

    var isAnimating      by mutableStateOf(false)
    var isTrajectoryMode by mutableStateOf(false)

    // 1) all saved rocket configs
    private val _rocketConfigList = MutableStateFlow<List<RocketConfig>>(emptyList())
    val rocketConfigList: StateFlow<List<RocketConfig>> = _rocketConfigList

    // 2) which one is “active”
    private val _selectedConfig = MutableStateFlow<RocketConfig?>(null)
    val selectedConfig: StateFlow<RocketConfig?> = _selectedConfig

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
            rocketConfigRepository
                .getAllRocketConfigs()
                .collect { list ->
                    _rocketConfigList.value = list
                    if (_selectedConfig.value == null && list.isNotEmpty()) {
                        _selectedConfig.value = list.first()
                    }
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

    /** Allow null here: elevation pending until terrain query returns */
    fun updateLastVisited(lat: Double, lon: Double, elevation: Double?) {
        viewModelScope.launch {
            try {
                val exists = launchSitesRepository.checkIfSiteExists("Last Visited")
                val site = LaunchSite(
                    uid = lastVisited.value?.uid ?: 0,
                    latitude = lat,
                    longitude = lon,
                    name = "Last Visited",
                    elevation = elevation   // now nullable column
                )
                if (exists && lastVisited.value != null) {
                    launchSitesRepository.update(site)
                } else {
                    launchSitesRepository.insert(site)
                }
            } catch (e: SQLiteConstraintException) {
                _uiState.value = HomeScreenUiState.Error(
                    "${e.message ?: "Unknown error"} for Last Visited"
                )
            }
        }
    }

    fun updateNewMarker(lat: Double, lon: Double, elevation: Double?) {
        viewModelScope.launch {
            try {
                val exists = launchSitesRepository.checkIfSiteExists("New Marker")
                val site = LaunchSite(
                    uid = newMarker.value?.uid ?: 0,
                    latitude = lat,
                    longitude = lon,
                    name = "New Marker",
                    elevation = elevation
                )
                if (exists && newMarker.value != null) {
                    launchSitesRepository.update(site)
                } else {
                    launchSitesRepository.insert(site)
                }
            } catch (e: Exception) {
                _uiState.value =
                    HomeScreenUiState.Error("Error saving marker elevation: ${e.message}")
            }
        }
    }

    /** Change edit/add APIs to accept nullable elevation too */
    fun editLaunchSite(siteId: Int, lat: Double, lon: Double, elevation: Double?, name: String) {
        viewModelScope.launch {
            val exists = launchSitesRepository.checkIfSiteExists(name)
            if (exists) {
                _updateStatus.value =
                    UpdateStatus.Error("Launch site with this name already exists")
            } else {
                launchSitesRepository.update(
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

    fun loadMockTrajectory() {
        val baseLat = 59.9431
        val baseLon = 10.7185
        val nPoints = 100   // now 100 samples

        val raw = (0 until nPoints).map { i ->
            val t = i / (nPoints - 1).toDouble()   // 0.0 .. 1.0
            // interpolate Lat/Lon along a small NE vector
            val lat   = baseLat + 0.010 * t       // make it larger
            val lon   = baseLon + 0.010 * t       // make it larger
            // altitude: sin(π t) * 5000 → peaks at 5000m
            val alt   = sin(Math.PI * t) * 5000.0
            // speed: same shape, you can keep 500 or bump it
            val speed = sin(Math.PI * t) * 500.0
            doubleArrayOf(lat, lon, alt, speed)
        }

        // Map into your StateFlow format: RealVector(lon, lat, alt) → speed
        val list: List<Pair<RealVector, Double>> = raw.map { arr ->
            val lat   = arr[0]
            val lon   = arr[1]
            val alt   = arr[2]
            val speed = arr[3]
            ArrayRealVector(doubleArrayOf(lon, lat, alt)) to speed
        }

        _trajectoryPoints.value = list
        isAnimating      = true
        isTrajectoryMode = true
    }

    // start “real” sim
    fun startTrajectory() {
        val cfg = _selectedConfig.value ?: return
        viewModelScope.launch {
            // build initial Vector
            val (lat, lon) = _coordinates.value
            val elev       = _lastVisited.value?.elevation ?: 0.0
            val initial = ArrayRealVector(doubleArrayOf(lon, lat, elev))

            // this returns List<Pair<RealVector, Double>>
            val traj = TrajectoryCalculator(isobaricInterpolator)
                .calculateTrajectory(
                    initialPosition            = initial,
                    launchAzimuth              = cfg.launchAzimuth,
                    launchPitch                = cfg.launchPitch,
                    launchRailLength           = cfg.launchRailLength,
                    wetMass                    = cfg.wetMass,
                    dryMass                    = cfg.dryMass,
                    burnTime                   = cfg.burnTime,
                    thrust                     = cfg.thrust,
                    stepSize                   = cfg.stepSize,
                    crossSectionalArea         = cfg.crossSectionalArea,
                    dragCoefficient            = cfg.dragCoefficient,
                    parachuteCrossSectionalArea= cfg.parachuteCrossSectionalArea,
                    parachuteDragCoefficient   = cfg.parachuteDragCoefficient
                )

            // assign directly—no extra `to 0.0`
            _trajectoryPoints.value = traj

            isAnimating      = true
            isTrajectoryMode = true
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
                launchSitesRepository.insert(
                    LaunchSite(latitude = lat, longitude = lon, elevation = elevation, name = name)
                )
                _updateStatus.value = UpdateStatus.Success
            } catch (e: SQLiteConstraintException) {
                _updateStatus.value =
                    UpdateStatus.Error("Launch site with this name already exists")
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

    /** Called when the user taps your “⚙️ Rocket Configs” button */
    fun selectConfig(config: RocketConfig) {
        _selectedConfig.value = config
    }

    /** Start the trajectory using the currently selected config */
    /*fun startTrajectory() {
        val cfg = _selectedConfig.value
            ?: return  // optionally show an error “please pick a config first”

        viewModelScope.launch {
            // build initial “RealVector” from center + elevation
            val (lat, lon) = coordinates.value
            val elev = lastVisited.value?.elevation ?: 0.0
            val initial = ArrayRealVector(doubleArrayOf(lat, lon, elev))

            trajectoryPoints = TrajectoryCalculator(isobaricInterpolator)
                .calculateTrajectory(
                    initialPosition = initial,
                    launchAzimuth = cfg.launchAzimuth,
                    launchPitch = cfg.launchPitch,
                    launchRailLength = cfg.launchRailLength,
                    wetMass = cfg.wetMass,
                    dryMass = cfg.dryMass,
                    burnTime = cfg.burnTime,
                    thrust = cfg.thrust,
                    stepSize = cfg.stepSize,
                    crossSectionalArea = cfg.crossSectionalArea,
                    dragCoefficient = cfg.dragCoefficient,
                    parachuteCrossSectionalArea = cfg.parachuteCrossSectionalArea,
                    parachuteDragCoefficient = cfg.parachuteDragCoefficient
                )

            isAnimating = true
        }
    */

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