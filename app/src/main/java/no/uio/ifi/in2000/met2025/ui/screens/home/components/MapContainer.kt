// MapContainer.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.MapView
import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun MapContainer(
    coordinates: Pair<Double, Double>,
    temporaryMarker: Point? = null,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point) -> Unit,
    onMarkerAnnotationClick: (Point) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        MapView(
            center = coordinates,
            temporaryMarker = temporaryMarker,
            launchSites = launchSites,
            mapViewportState = mapViewportState,
            modifier = Modifier.fillMaxSize(),
            showAnnotations = showAnnotations,
            onMapLongClick = onMapLongClick,
            onMarkerAnnotationClick = onMarkerAnnotationClick,
            onLaunchSiteMarkerClick = onLaunchSiteMarkerClick
        )
    }
}