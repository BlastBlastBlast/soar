package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
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
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import no.uio.ifi.in2000.met2025.R

@Composable
fun MarkerLabel(
    text: String,
    onClick: () -> Unit,
    fontSize: TextUnit = 10.sp
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = fontSize)
    }
}


/**
 * MapView is now configuration–friendly. All important inputs are passed as parameters:
 *
 * @param center a Pair (latitude, longitude) that defines the camera center.
 * @param temporaryMarker an optional Point representing a newly placed marker.
 * @param launchSites a list of saved launch sites.
 * @param showAnnotations toggles marker labels.
 * @param onMapLongClick invoked when the user long–presses on the map (passing the clicked Point).
 * @param onMarkerAnnotationClick invoked when a temporary marker label is tapped.
 * @param onLaunchSiteMarkerClick invoked when a saved launch site’s label is tapped.
 */
@Composable
fun MapView(
    center: Pair<Double, Double>,
    temporaryMarker: Point? = null,
    launchSites: List<LaunchSite> = emptyList(),
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point) -> Unit,
    onMarkerAnnotationClick: (Point) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {}
) {
    // Set up the map state with a default camera:
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val mapViewportState = rememberMapViewportState()

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            style = { MapStyle(style = Style.STANDARD) },
            mapState = mapState,
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point ->
                onMapLongClick(point)
                true
            }
        ) { // Trailing lambda: proper MapApplier is in scope.
            // ── Configure the location puck (this runs once and does not affect user panning/zooming) ──
            MapEffect(Unit) {
                this.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
            }
            // ── Draw the temporary marker and its label ──
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
                            onClick = { onMarkerAnnotationClick(point) }
                        )
                    }
                }
            }
            // ── Draw saved launch site markers and their labels ──
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
                            onClick = { onLaunchSiteMarkerClick(site) }
                        )
                    }
                }
            }
        }
    }
}}