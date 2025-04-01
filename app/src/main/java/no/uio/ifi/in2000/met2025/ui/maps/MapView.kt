package no.uio.ifi.in2000.met2025.ui.maps

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



@Composable
fun MapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    onMarkerPlaced: (Double, Double) -> Unit,
    onMarkerAnnotationClick: (Double, Double) -> Unit  // New callback
) {
    val mapViewportState = rememberMapViewportState()
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    var markerCoordinate by remember { mutableStateOf<Point?>(null) }
    var initialTransitionDone by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            style = { MapStyle(style = Style.STANDARD) },
            mapState = mapState,
            mapViewportState = mapViewportState,
            onMapLongClickListener = { point: Point ->
                markerCoordinate = point
                onMarkerPlaced(point.latitude(), point.longitude())
                true
            }
        ) {
            MapEffect(markerCoordinate) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }
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
            // Draw the red marker using PointAnnotation.
            markerCoordinate?.let { coordinate ->
                val markerImage = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = coordinate) {
                    iconImage = markerImage
                }
            }
            // Add a view annotation anchored to the marker.
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
                        // When tapped, call the callback with marker coordinates.
                        onMarkerAnnotationClick(coordinate.latitude(), coordinate.longitude())
                    }
                }
            }
        }
    }
}
