package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mapbox.geojson.Point
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
    //temporaryMarker: Point? = null,
    newMarker: LaunchSite?,
    newMarkerStatus: Boolean,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point) -> Unit,
    onMarkerAnnotationClick: (Point) -> Unit,
    onMarkerAnnotationLongPress: (Point) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {},
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit = {}
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
    var temporaryMarker : Point? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            //style = { MapStyle(style = Style.STANDARD) },
            style = { MapStyle ( style = "mapbox://styles/larswt/cm9ftfa5h00ix01s86li36n61") },
            mapState = mapState,
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point ->
                onMapLongClick(point)
                temporaryMarker = point
                true
            }
        ) {
            MapEffect(Unit) { mapView ->
                val locationPlugin = mapView.getPlugin("location") as? LocationComponentPlugin
                locationPlugin?.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
            }
            if (newMarkerStatus) {
                temporaryMarker?.let { point ->
                    val markerImage = rememberIconImage(
                        key = R.drawable.red_marker,
                        painter = painterResource(id = R.drawable.red_marker)
                    )
                    PointAnnotation(point = point) { iconImage = markerImage }
                    if (showAnnotations) {
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(point)
                                annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                                allowOverlap(true)
                            }
                        ) {
                            MarkerLabel(
                                name = "New Marker",
                                lat = "%.4f".format(point.latitude()),
                                lon = "%.4f".format(point.longitude()),
                                onClick = { onMarkerAnnotationClick(point) },
                                onDoubleClick = { },
                                onLongPress = { onMarkerAnnotationLongPress(point) }
                            )
                        }
                    }
                }
            }
            launchSites.filter { it.name != "Last Visited" && it.name != "New Marker"}.forEach { site ->
                val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                val markerImage = rememberIconImage(
                    key = "launchSite_${site.uid}",
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = sitePoint) { iconImage = markerImage }
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
                            onClick = { /* Optionally handle single tap on saved marker */ },
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
                            onLongPress = {
                                onSavedMarkerAnnotationLongPress(site)
                            }
                        )
                    }
                }
            }
        }
    }
}