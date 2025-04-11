package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun MarkerLabel(
    text: String,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    fontSize: TextUnit = 10.sp
) {
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() }
                )
            }
            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = fontSize)
    }
}


// MapView.kt
@Composable
fun MapView(
    center: Pair<Double, Double>,
    temporaryMarker: Point? = null,
    launchSites: List<LaunchSite>,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point) -> Unit,
    onMarkerAnnotationClick: (Point) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {}
) {
    // Use MapViewportState for camera control.
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    // A coroutine scope for camera animation.
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            style = { MapStyle(style = Style.STANDARD) },
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point ->
                onMapLongClick(point)
                true
            }
        ) {
            // Configure the location puck.
            MapEffect(Unit) { mapView ->
                val locationPlugin = mapView.getPlugin("location") as? LocationComponentPlugin
                locationPlugin?.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
            }
            // Draw the temporary marker and its label.
            temporaryMarker?.let { point ->
                val markerImage = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = point) {
                    iconImage = markerImage
                }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(point)
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            text = "New Marker\nLat: ${"%.4f".format(point.latitude())}\nLon: ${"%.4f".format(point.longitude())}",
                            onClick = { onMarkerAnnotationClick(point) },
                            onDoubleClick = { /* Optionally handle double tap on temporary marker */ }
                        )
                    }
                }
            }
            // Draw saved launch site markers and their labels.
            launchSites.filter { it.name != "Last Visited" }.forEach { site ->
                val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                val markerImage = rememberIconImage(
                    key = "launchSite_${site.uid}",
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = sitePoint) {
                    iconImage = markerImage
                }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(sitePoint)
                            annotationAnchor { anchor(ViewAnnotationAnchor.TOP) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            text = "${site.name}\nLat: ${"%.4f".format(site.latitude)}\nLon: ${"%.4f".format(site.longitude)}",
                            onClick = { /* Single tap behavior if needed */ },
                            onDoubleClick = {
                                scope.launch {
                                    // Animate the camera to this marker using MapViewportState.
                                    mapViewportState.easeTo(
                                        cameraOptions {
                                            center(sitePoint)
                                            zoom(14.0)
                                            // You can also set pitch and bearing here if desired:
                                            pitch(45.0)
                                            //bearing(0.0)
                                        },
                                        //duration = 1000L // Duration in milliseconds.
                                    )
                                }
                                // Invoke additional callback behavior if needed.
                                onLaunchSiteMarkerClick(site)
                            }
                        )
                    }
                }
            }
        }
    }
}
