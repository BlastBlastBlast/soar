package no.uio.ifi.in2000.met2025.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import no.uio.ifi.in2000.met2025.R

@Composable
fun MapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    onMarkerPlaced: (Double, Double) -> Unit
) {
    // Create viewport and map states.
    val mapViewportState = rememberMapViewportState()
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)  // Start with north‑up.
        }
    }
    var markerCoordinate by remember { mutableStateOf<Point?>(null) }
    // Flag to run the initial camera update only once.
    var initialTransitionDone by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            // Set the map style to satellite.
            style = { MapStyle(style = Style.STANDARD) },
            mapState = mapState,
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point: Point ->
                markerCoordinate = point
                onMarkerPlaced(point.latitude(), point.longitude())
                true
            }
        ) {
            // Configure the location component with a rotating puck.
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
                // Do a one‑time camera update to lock bearing to north‑up.
                if (!initialTransitionDone) {
                    mapView.mapboxMap.setCamera(
                        cameraOptions {
                            center(Point.fromLngLat(longitude, latitude))
                            zoom(12.0)
                            pitch(0.0)
                            bearing(0.0)
                        }
                    )
                    initialTransitionDone = true
                }
            }
            // If a marker coordinate is set, add a point annotation.
            markerCoordinate?.let { coordinate ->
                val markerImage = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = coordinate) {
                    iconImage = markerImage
                }
            }
        }
    }
}