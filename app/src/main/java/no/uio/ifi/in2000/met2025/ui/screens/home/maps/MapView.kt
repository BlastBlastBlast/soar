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
import androidx.compose.ui.unit.dp
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
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.flow.first
import no.uio.ifi.in2000.met2025.R

@Composable
fun MarkerLabel(coordinate: Point, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Lat: ${"%.4f".format(coordinate.latitude())}\nLon: ${"%.4f".format(coordinate.longitude())}",
            color = Color.White
        )
    }
}

// TODO: Nav to marker location on launch, not post launch.

@Composable
fun MapView(
    latitude: Double,
    longitude: Double,
    initialMarkerCoordinate: Point? = null,
    modifier: Modifier = Modifier,
    onMarkerPlaced: (Double, Double) -> Unit,
    onMarkerAnnotationClick: (Double, Double) -> Unit
) {
    val mapViewportState = rememberMapViewportState()
    val mapState = rememberMapState {
        // Set the default camera to the user's location.
        cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    // Reinitialize marker state (and initialTransitionDone) whenever initialMarkerCoordinate changes.
    var markerCoordinate by remember(initialMarkerCoordinate) { mutableStateOf(initialMarkerCoordinate) }
    var initialTransitionDone by remember(initialMarkerCoordinate) { mutableStateOf(false) }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            style = { MapStyle(style = Style.STANDARD) },
            mapState = mapState,
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point: Point ->
                // When the user long-presses, update the marker and notify the callback.
                markerCoordinate = point
                onMarkerPlaced(point.latitude(), point.longitude())
                true
            }
        ) {
            MapEffect(markerCoordinate, initialMarkerCoordinate) { mapView ->
                // Update location puck settings.
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
                // On initial load (only once) and if a last visited coordinate exists,
                // center the camera on it while preserving the current zoom level.
                if (!initialTransitionDone && initialMarkerCoordinate != null) {
                    val currentCameraState = mapView.mapboxMap.cameraState
                    mapView.mapboxMap.setCamera(
                        cameraOptions {
                            center(initialMarkerCoordinate)
                            // Preserve the current zoom level instead of hardcoding zoom(12.0)
                            zoom(currentCameraState.zoom)
                            pitch(currentCameraState.pitch)
                            bearing(currentCameraState.bearing)
                        }
                    )
                    initialTransitionDone = true
                }
            }

            // Draw the red marker if markerCoordinate exists.
            markerCoordinate?.let { coordinate ->
                val markerImage = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = coordinate) {
                    iconImage = markerImage
                }
            }
            // Draw a view annotation for the marker.
            markerCoordinate?.let { coordinate ->
                ViewAnnotation(
                    options = viewAnnotationOptions {
                        geometry(coordinate)
                        annotationAnchor {
                            anchor(ViewAnnotationAnchor.BOTTOM)
                            offsetY(60.0)
                        }
                        allowOverlap(true)
                    }
                ) {
                    MarkerLabel(coordinate = coordinate) {
                        onMarkerAnnotationClick(coordinate.latitude(), coordinate.longitude())
                    }
                }
            }
        }
    }
}
