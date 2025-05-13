package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.modelLayer
import com.mapbox.maps.extension.style.layers.generated.skyLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ModelType
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.layers.properties.generated.SkyType
import com.mapbox.maps.extension.style.model.addModel
import com.mapbox.maps.extension.style.model.model
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.domain.RocketState
import org.apache.commons.math3.linear.RealVector


/*
 * This file defines a Composable MapView for displaying launch sites and simulated rocket trajectories using Mapbox.
 * Main functionality:
 *  - Render an interactive Mapbox map with markers for saved launch sites and a user-added marker.
 *  - Fetch and display terrain elevations via Mapbox DEM.
 *  - Draw and animate a 3D model trajectory of a rocket flight.
 * Special notes:
 *  - Expects trajectoryPoints as a list of (RealVector lat/lon/alt, unused, RocketState).
 *  - Uses MapView.getElevation() for DEM retrieval; requires a short delay for terrain loading.
 */


/**
 * Displays a Mapbox map with:
 *  - Centered camera on `center` coordinates.
 *  - A "new" marker for user placement.
 *  - Saved launch site markers with elevation labels.
 *  - 3D trajectory models representing rocket states and flight.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun MapView(
    center: Pair<Double, Double>,
    newMarker: LaunchSite?,
    newMarkerStatus: Boolean,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point, Double?) -> Unit,
    onMarkerAnnotationClick: (Point, Double?) -> Unit,
    onMarkerAnnotationLongPress: (Point, Double?) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {},
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit = {},
    onSiteElevation: (Int, Double) -> Unit,
    trajectoryPoints: List<Triple<RealVector, Double, RocketState>>, // sim points: (lat,lon,altAboveLaunchDatum)
    isAnimating: Boolean,
    onAnimationEnd: () -> Unit,
    styleReloadTrigger: Int
) {
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0); pitch(0.0); bearing(0.0)
        }
    }
    val scope = rememberCoroutineScope()
    var requestedPts by remember { mutableStateOf(setOf<Point>()) }
    var temporaryMarker: Point? by rememberSaveable { mutableStateOf(null) }
    var markerElevation: Double? by rememberSaveable { mutableStateOf(null) }
    var mapViewRef: MapView? by remember { mutableStateOf(null) }
    var baseStyleLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(styleReloadTrigger) {
        baseStyleLoaded = false
    }

    /**
     * Fetches true terrain elevation for the given point via Mapbox DEM and
     * calls onSiteElevation(siteId, elevation) to store the result.
     * Delays briefly to ensure DEM source is loaded.
     */
    suspend fun fetchTrueElevationAndStore(siteId: Int, pt: Point) {
        // Allow Mapbox terrain tiles to initialize
        delay(500)
        val dem = mapViewRef
            ?.mapboxMap
            ?.getElevation(pt)
        if (dem != null) {
            onSiteElevation(siteId, dem)
        }
        markerElevation = dem
    }

    LaunchedEffect(newMarker) {
        newMarker?.let { site ->
            temporaryMarker = Point.fromLngLat(site.longitude, site.latitude)
            markerElevation = site.elevation
            if (site.elevation == null && mapViewRef != null) {
                fetchTrueElevationAndStore(site.uid, temporaryMarker!!)
            }
        }
    }


    Box(modifier.fillMaxSize()) {
        //key(styleReloadTrigger) {
            MapboxMap(
                modifier             = modifier.fillMaxSize(),
                mapState             = mapState,
                mapViewportState     = mapViewportState,
                onMapLongClickListener = { pt -> onMapLongClick(pt, null); true },
                style                = { /* no-op: we load style imperatively below */ }
            ) {
                // Base style + DEM + terrain + sky + globe
                MapEffect(mapViewRef, styleReloadTrigger) { mv ->
                    mapViewRef = mv
                    if (!baseStyleLoaded) {
                        baseStyleLoaded = true
                        mv.mapboxMap.loadStyle(styleExtension = style(Style.SATELLITE_STREETS) {
                            +rasterDemSource("dem") {
                                url("mapbox://mapbox.mapbox-terrain-dem-v1"); tileSize(
                                512
                            )
                            }
                            +terrain("dem") { exaggeration(1.0) }
                            +skyLayer("sky") { skyType(SkyType.ATMOSPHERE) }
                            +projection(ProjectionName.GLOBE)
                        }) {
                            (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                                enabled = true
                                locationPuck = createDefault2DPuck(withBearing = true)
                                puckBearing = PuckBearing.COURSE
                                puckBearingEnabled = true
                            }
                        }
                    }
                }


                // Remove existing trajectory when no points
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) {
                        mv.mapboxMap.getStyle { style ->
                            // Remove all "traj-lyr-*" layers
                            style.styleLayers
                                .map { it.id }                              // StyleObjectInfo.id
                                .filter { it.startsWith("traj-lyr-") }
                                .forEach { layerId ->
                                    style.removeStyleLayer(layerId)           // removeStyleLayer(String)
                                }

                            // Remove all "traj-src-*" sources
                            style.styleSources
                                .map { it.id }
                                .filter { it.startsWith("traj-src-") }
                                .forEach { sourceId ->
                                    style.removeStyleSource(sourceId)         // removeStyleSource(String)
                                }
                        }
                    }
                }
                // Draw trajectory points as 3D models
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) return@MapEffect

                    // Offset model rendering
                    val firstFreeFlightIdx = trajectoryPoints
                        .indexOfFirst { it.third == RocketState.FREE_FLIGHT }
                        .takeIf { it >= 0 } ?: 0
                    val rocketOffset    = 5
                    val rocketModelIdx  = firstFreeFlightIdx + rocketOffset

                    val parachuteOffset   = 1 
                    val firstParachuteIdx = trajectoryPoints
                        .indexOfFirst { it.third == RocketState.PARACHUTE_DEPLOYED }
                        .takeIf { it >= 0 }
                    val parachuteModelIdx = firstParachuteIdx?.plus(parachuteOffset)

                    // Fallback terrain elevation at launch
                    val launchElev = trajectoryPoints.first().first.getEntry(2)

                    // Draw trajectory points
                    trajectoryPoints.forEachIndexed { idx, (vec, _, state) ->
                        // Limit amount of parachute points
                        if (state == RocketState.PARACHUTE_DEPLOYED
                            && idx != parachuteModelIdx
                            && idx % 10 != 0) return@forEachIndexed

                        val lat    = vec.getEntry(0)
                        val lon    = vec.getEntry(1)
                        val absAlt = vec.getEntry(2)
                        val terrain = mv.mapboxMap
                            .getElevation(Point.fromLngLat(lon, lat))
                            ?: launchElev
                        val relAlt = absAlt - terrain

                        val modelUri = when {
                            idx == rocketModelIdx -> "asset://Rocket.glb"
                            parachuteModelIdx != null && idx == parachuteModelIdx -> "asset://parachute_offset.glb"
                            else -> when (state) {
                                RocketState.ON_LAUNCH_RAIL     -> "asset://PurpleIso.glb"
                                RocketState.THRUSTING          -> "asset://RedIso.glb"
                                RocketState.FREE_FLIGHT        -> "asset://OrangeIso.glb"
                                RocketState.PARACHUTE_DEPLOYED -> "asset://BlueIso.glb"
                                RocketState.LANDED             -> "asset://GreenIso.glb"
                            }
                        }

                        // Decide scale for rocket and parachute
                        val scaleVec = when {
                            idx == rocketModelIdx -> listOf(200.0, 200.0, 200.0)
                            parachuteModelIdx != null && idx == parachuteModelIdx -> listOf(60.0, 60.0, 60.0)
                            else -> listOf(5.0, 5.0, 5.0)
                        }

                        val modelId  = "traj-model-$idx"
                        val sourceId = "traj-src-$idx"
                        val layerId  = "traj-lyr-$idx"

                        mv.mapboxMap.getStyle { style ->
                            if (!style.styleLayers.any { it.id == modelId }) {
                                mv.mapboxMap.addModel(model(modelId) {
                                    uri(modelUri)
                                })
                                println("➤ added MODEL $modelId → $modelUri")
                            }
                            if (!style.styleSources.any { it.id == sourceId }) {
                                style.addSource(geoJsonSource(sourceId) {
                                    data(FeatureCollection.fromFeatures(arrayOf(
                                        Feature.fromGeometry(Point.fromLngLat(lon, lat, relAlt))
                                    )).toJson())
                                })
                            }
                            if (!style.styleLayers.any { it.id == layerId }) {
                                style.addLayer(modelLayer(layerId, sourceId) {
                                    modelId(modelId)
                                    modelType(ModelType.COMMON_3D)
                                    modelScale(scaleVec)
                                    modelTranslation(listOf(0.0, 0.0, relAlt))
                                    modelCastShadows(true)
                                    modelReceiveShadows(true)
                                })
                                if (style.styleLayers.any { it.id == layerId }) {
                                    println("ModelLayer $layerId added successfully.")
                                } else {
                                    println("Failed to add ModelLayer $layerId.")
                                }
                            }
                        }
                    }
                }

                // Animate camera over the lifted trajectory
                MapEffect(trajectoryPoints to isAnimating) { mv ->
                    if (!isAnimating || trajectoryPoints.isEmpty()) return@MapEffect
                    (mv.context as ComponentActivity).lifecycleScope.launch {
                        var prev: Point? = null
                        trajectoryPoints.forEach { (vec, _) ->
                            val lon = vec.getEntry(1)
                            val lat = vec.getEntry(0)
                            val alt = vec.getEntry(2)
                            val p = Point.fromLngLat(lon, lat, alt)
                            val bearing = prev?.let {
                                calculateBearing(it.longitude(), it.latitude(), lon, lat)
                            } ?: 0.0
                            mv.mapboxMap.easeTo(
                                CameraOptions.Builder()
                                    .center(p)
                                    .pitch(70.0)
                                    .bearing(bearing)
                                    .zoom(12.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions { duration(200L) }
                            )
                            prev = p
                        }
                        onAnimationEnd()
                    }
                }

                // MapView ref & enable location puck
                MapEffect(Unit) { mv ->
                    mapViewRef = mv
                    (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                }


                // Draw the “new” marker
                // Null check needed for first launch of app
                if (newMarkerStatus && newMarker != null) {
                    key(newMarker.uid to newMarker.name) {
                        val icon = rememberIconImage(
                            key = R.drawable.red_marker,
                            painter = painterResource(R.drawable.red_marker)
                        )
                        val pt =
                            temporaryMarker ?: Point.fromLngLat(
                                newMarker.longitude,
                                newMarker.latitude
                            )
                        PointAnnotation(point = pt) { iconImage = icon }
                        if (showAnnotations) {
                            ViewAnnotation(
                                options = viewAnnotationOptions {
                                    geometry(pt)
                                    annotationAnchor {
                                        anchor(ViewAnnotationAnchor.BOTTOM).offsetY(
                                            60.0
                                        )
                                    }
                                    allowOverlap(true)
                                }
                            ) {
                                MarkerLabel(
                                    name = newMarker.name,
                                    lat = "%.4f".format(pt.latitude()),
                                    lon = "%.4f".format(pt.longitude()),
                                    elevation = markerElevation?.let { "%.1f m".format(it) },
                                    isLoadingElevation = markerElevation == null, // Shows loader
                                    onClick = { onMarkerAnnotationClick(pt, markerElevation) },
                                    onLongPress = {
                                        onMarkerAnnotationLongPress(
                                            pt,
                                            markerElevation
                                        )
                                    },
                                    onDoubleClick = {
                                        scope.launch {
                                            mapViewportState.easeTo(
                                                cameraOptions {
                                                    center(pt)
                                                    zoom(14.0)
                                                    pitch(0.0)
                                                    bearing(0.0)
                                                },
                                                MapAnimationOptions.mapAnimationOptions {
                                                    duration(
                                                        1000L
                                                    )
                                                }
                                            )
                                        }
                                        onLaunchSiteMarkerClick(newMarker)
                                    }
                                )

                            }
                        }
                    }
                }

                // Draw all other launch sites
                launchSites
                    .filter { it.name !in listOf("Last Visited", "New Marker") }
                    .forEach { site ->
                        val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                        val siteImage = rememberIconImage(
                            key = "launchSite_${site.uid}",
                            painter = painterResource(R.drawable.red_marker)
                        )

                        PointAnnotation(point = sitePoint) { iconImage = siteImage }

                        if (showAnnotations) {
                            key(site.uid to site.name) {
                                ViewAnnotation(
                                    options = viewAnnotationOptions {
                                        geometry(sitePoint)
                                        annotationAnchor {
                                            anchor(ViewAnnotationAnchor.BOTTOM).offsetY(
                                                60.0
                                            )
                                        }
                                        allowOverlap(true)
                                    }
                                ) {
                                    MarkerLabel(
                                        name = site.name,
                                        lat = "%.4f".format(site.latitude),
                                        lon = "%.4f".format(site.longitude),
                                        elevation = site.elevation?.let { "%.1f m".format(it) }
                                            ?: "—",
                                        onClick = { /* tap‐noop */ },
                                        onDoubleClick = {
                                            scope.launch {
                                                mapViewportState.easeTo(
                                                    cameraOptions {
                                                        center(sitePoint)
                                                        zoom(14.0)
                                                        pitch(0.0)
                                                        bearing(0.0)
                                                    },
                                                    MapAnimationOptions.mapAnimationOptions {
                                                        duration(
                                                            1000L
                                                        )
                                                    }
                                                )
                                            }
                                            onLaunchSiteMarkerClick(site)
                                        },
                                        onLongPress = { onSavedMarkerAnnotationLongPress(site) }
                                    )
                                }
                            }
                        }

                        LaunchedEffect(site.uid, site.elevation) {
                            if (site.elevation == null
                                && !requestedPts.contains(sitePoint)
                                && mapViewRef != null
                            ) {
                                requestedPts = requestedPts + sitePoint
                                val dem = mapViewRef!!
                                    .mapboxMap
                                    .getElevation(sitePoint)
                                dem?.let { onSiteElevation(site.uid, it) }
                            }
                        }
                    }
            }
        }
    }
//}

