package no.uio.ifi.in2000.met2025.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.dsl.cameraOptions

@Composable
fun MapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    onMarkerPlaced: (Double, Double) -> Unit
) {
    // Set up your MapState with the initial camera options.
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    var markerCoordinate by remember { mutableStateOf<Point?>(null) }

    Box(modifier = modifier) {
        // Instead of manually capturing the MapboxMap instance, pass a long click listener.
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapState = mapState,
            onMapLongClickListener = { point: Point ->
                // Here the callback already returns a geographic coordinate.
                markerCoordinate = point
                onMarkerPlaced(point.latitude(), point.longitude())
                true  // consume the event
            }
        )
        // Optionally, overlay a simple marker label.
        markerCoordinate?.let {
            Text(
                text = "Marker",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
