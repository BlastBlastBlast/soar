package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
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
import org.apache.commons.math3.linear.RealVector

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

    // Trajectory integration
    trajectoryPoints: List<Pair<RealVector, Double>> = emptyList(),
    isAnimating: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    // 1) Map state & scope
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0); pitch(0.0); bearing(0.0)
        }
    }
    val scope = rememberCoroutineScope()
    var requestedPts by remember { mutableStateOf(setOf<Point>()) }
    // 2) Local state for the “new marker”
    var temporaryMarker: Point? by rememberSaveable { mutableStateOf(null) }
    var mapViewRef: MapView? by remember { mutableStateOf(null) }
    var markerElevation: Double? by rememberSaveable { mutableStateOf(null) }


    // ← NEW: helper to fetch & store DEM elevation
    suspend fun fetchTrueElevationAndStore(siteId: Int, pt: Point) {
        // give Mapbox a frame to load terrain
        delay(500)
        val dem = mapViewRef
            ?.mapboxMap
            ?.getElevation(pt)      // returns Double?
        if (dem != null) {
            onSiteElevation(siteId, dem)
        }
        markerElevation = dem
    }

    // 3) Sync VM-driven newMarker into temporaryMarker
    LaunchedEffect(newMarker) {
        newMarker?.let { site ->
            temporaryMarker = Point.fromLngLat(site.longitude, site.latitude)
            markerElevation = site.elevation  // might be null
            if (site.elevation == null && mapViewRef != null) {
                fetchTrueElevationAndStore(site.uid, temporaryMarker!!)
            }
        }
    }

    /*
        mapViewRef!!.mapboxMap.queryTerrainElevation(
      ScreenCoordinate(pt.x, pt.y),
      ElevationUnit.METER
    ) { result ->
      result.value?.let { dem ->
        onSiteElevation(siteId, dem)
        markerElevation = dem
      }
    }
    */


    Box(modifier) {
        // 4) Core MapboxMap
        MapboxMap(
            modifier             = Modifier.fillMaxSize(),
            style                = { MapStyle("mapbox://styles/larswt/cm9ftfa5h00ix01s86li36n61") },
            mapState             = mapState,
            mapViewportState     = mapViewportState,
            onMapLongClickListener = { pt ->
                onMapLongClick(pt, null)
                temporaryMarker = pt
                true
            }
        ) {
            // 5) Capture MapView reference & enable location puck
            MapEffect(Unit) { mv ->
                mapViewRef = mv
                (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                    locationPuck       = createDefault2DPuck(withBearing = true)
                    enabled            = true
                    puckBearing        = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
            }


            // 6) Draw the “new” marker
            if (newMarkerStatus) {
                val icon = rememberIconImage(key = R.drawable.red_marker, painter = painterResource(R.drawable.red_marker))
                val pt   = temporaryMarker ?: Point.fromLngLat(newMarker!!.longitude, newMarker.latitude)
                PointAnnotation(point = pt) { iconImage = icon }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(pt)
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM).offsetY(60.0) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            name               = newMarker?.name ?: "New Marker",
                            lat                = "%.4f".format(pt.latitude()),
                            lon                = "%.4f".format(pt.longitude()),
                            elevation          = markerElevation?.let { "%.1f m".format(it) },
                            isLoadingElevation = markerElevation == null,   // show spinner when null
                            onClick            = { onMarkerAnnotationClick(pt, markerElevation) },
                            onLongPress        = { onMarkerAnnotationLongPress(pt, markerElevation) },
                            onDoubleClick      = { /* no-op */ }
                            )

                    }
                }
            }

            // 7) Draw all other launch sites
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
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(sitePoint)
                                annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM).offsetY(60.0) }
                                allowOverlap(true)
                            }
                        ) {
                            MarkerLabel(
                                name = site.name,
                                lat = "%.4f".format(site.latitude),
                                lon = "%.4f".format(site.longitude),
                                elevation = site.elevation?.let { "%.1f m".format(it) } ?: "—",
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
                                            MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                                        )
                                    }
                                    onLaunchSiteMarkerClick(site)
                                },
                                onLongPress = { onSavedMarkerAnnotationLongPress(site) }
                            )
                        }
                    }

                    // only fetch terrain elevation once, and only if it's still null
                    LaunchedEffect(site.uid, site.elevation) {
                        if (site.elevation == null
                            && !requestedPts.contains(sitePoint)
                            && mapViewRef != null
                        ) {
                            // mark as requested so we don't repeat
                            requestedPts = requestedPts + sitePoint

                            // synchronous elevation query
                            val dem = mapViewRef!!
                                .mapboxMap
                                .getElevation(sitePoint)   // returns Double?
                            dem?.let { onSiteElevation(site.uid, it) }
                        }
                    }
                }

            // 8) Trajectory overlay + camera animation INSIDE MapboxMap
            MapEffect(key1 = trajectoryPoints, key2 = isAnimating) { mv ->
                val mbMap = mv.mapboxMap
                mbMap.getStyle { style ->
                    // a) add source + layer once
                    if (style.getSource("traj-src") == null) {
                        style.addSource(
                            geoJsonSource("traj-src") { lineMetrics(true) }
                        )
                        style.addLayer(
                            lineLayer("traj-layer", "traj-src") {
                                lineColor("#FF0000"); lineWidth(4.0); lineJoin(LineJoin.ROUND)
                            }
                        )
                    }
                    // b) update GeoJSON data
                    if (trajectoryPoints.isNotEmpty()) {
                        val pts = trajectoryPoints.map { (vec, _) ->
                            Point.fromLngLat(vec.getEntry(1), vec.getEntry(0), vec.getEntry(2))
                        }
                        style.getSourceAs<GeoJsonSource>("traj-src")
                            ?.featureCollection(
                                FeatureCollection.fromFeatures(arrayOf(
                                    Feature.fromGeometry(LineString.fromLngLats(pts))
                                ))
                            )
                    }
                    // c) animate camera
                    if (isAnimating && trajectoryPoints.isNotEmpty()) {
                        (mv.context as ComponentActivity).lifecycleScope.launch {
                            var prev: Point? = null
                            for ((vec, _) in trajectoryPoints) {
                                val lng = vec.getEntry(1)
                                val lat = vec.getEntry(0)
                                val bearing = prev?.let {
                                    calculateBearing(it.longitude(), it.latitude(), lng, lat)
                                } ?: 0.0
                                mbMap.easeTo(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(lng, lat))
                                        .pitch(60.0).bearing(bearing).zoom(14.0).build(),
                                    MapAnimationOptions.mapAnimationOptions { duration(200L) }
                                )
                                prev = Point.fromLngLat(lng, lat)
                            }
                            onAnimationEnd()
                        }
                    }
                }
            }
        }
    }
}


/** Compute bearing in degrees from (lon1,lat1) → (lon2,lat2) */
private fun calculateBearing(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double {
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δλ = Math.toRadians(lon2 - lon1)
    val y  = Math.sin(Δλ) * Math.cos(φ2)
    val x  = Math.cos(φ1) * Math.sin(φ2) -
            Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ)
    return Math.toDegrees(Math.atan2(y, x))
}