// MapContainer.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.MapView

@Composable
fun MapContainer(
    coordinates: Pair<Double, Double>,
    initialMarkerCoordinate: Point? = null,
    modifier: Modifier = Modifier,
    onMarkerPlaced: (Double, Double) -> Unit,
    onMarkerAnnotationClick: (Double, Double) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        MapView(
            latitude = coordinates.first,
            longitude = coordinates.second,
            initialMarkerCoordinate = initialMarkerCoordinate,
            modifier = Modifier.fillMaxSize(),
            onMarkerPlaced = onMarkerPlaced,
            onMarkerAnnotationClick = onMarkerAnnotationClick
        )
    }
}
