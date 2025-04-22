package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

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
    onSiteElevation: (Int, Double) -> Unit
) {
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val scope = rememberCoroutineScope()
    var temporaryMarker: Point? by rememberSaveable { mutableStateOf(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerElevation: Double? by rememberSaveable { mutableStateOf(null) }

    Box(modifier = modifier) {
        MapboxMap(
            modifier             = Modifier.fillMaxSize(),
            style                = { MapStyle(style = "mapbox://styles/larswt/cm9ftfa5h00ix01s86li36n61") },
            mapState             = mapState,
            mapViewportState     = mapViewportState,
            onMapLongClickListener = { point ->
                // 1) fetch DEM elevation
                val elev = mapViewRef
                    ?.mapboxMap
                    ?.getElevation(point)
                markerElevation = elev

                // 2) invoke your callback with both point + elev
                onMapLongClick(point, elev)

                // 3) store the pin in state
                temporaryMarker = point
                true
            }
        ) {
            MapEffect(Unit) { mapView ->
                mapViewRef = mapView
                (mapView.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                    locationPuck        = createDefault2DPuck(withBearing = true)
                    enabled             = true
                    puckBearing         = PuckBearing.COURSE
                    puckBearingEnabled  = true
                }
            }

            // — NEW TEMPORARY MARKER —
            if (newMarkerStatus && temporaryMarker != null) {
                val pt = temporaryMarker!!
                val icon = rememberIconImage(
                    key     = R.drawable.red_marker,
                    painter = painterResource(R.drawable.red_marker)
                )
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
                            name      = "New Marker",
                            lat       = "%.4f".format(pt.latitude()),
                            lon       = "%.4f".format(pt.longitude()),
                            elevation = markerElevation?.let { "%.1f m".format(it) },
                            onClick   = { onMarkerAnnotationClick(pt, markerElevation) },
                            onDoubleClick = { /* no-op */ },
                            onLongPress   = { onMarkerAnnotationLongPress(pt, markerElevation) }
                        )
                    }
                }
            }

            // — SAVED LAUNCH SITES —
            launchSites
                .filter { it.name !in listOf("Last Visited", "New Marker") }
                .forEach { site ->
                    val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                    val icon = rememberIconImage(
                        key     = "launchSite_${site.uid}",
                        painter = painterResource(R.drawable.red_marker)
                    )

                    // back‑fill elevation once if still zero
                    LaunchedEffect(site.uid, site.elevation) {
                        if (site.elevation == 0.0) {
                            mapViewRef
                                ?.mapboxMap
                                ?.getElevation(sitePoint)
                                ?.let { elev -> onSiteElevation(site.uid, elev) }
                        }
                    }

                    PointAnnotation(point = sitePoint) { iconImage = icon }

                    if (showAnnotations) {
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(sitePoint)
                                annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM).offsetY(60.0) }
                                allowOverlap(true)
                            }
                        ) {
                            MarkerLabel(
                                name      = site.name,
                                lat       = "%.4f".format(site.latitude),
                                lon       = "%.4f".format(site.longitude),
                                elevation = "%.1f m".format(site.elevation),
                                onClick   = { /* no-op: use onDoubleClick */ },
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
                                onLongPress   = { onSavedMarkerAnnotationLongPress(site) }
                            )
                        }
                    }
                }
        }
    }
}
