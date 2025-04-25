// MapContainer.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.MapView
import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

// MapContainer.kt

@Composable
fun MapContainer(
    coordinates: Pair<Double, Double>,
    newMarker: LaunchSite?,
    newMarkerStatus: Boolean,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,

    // ↑ changed these three to take an optional elevation
    onMapLongClick: (Point, Double?) -> Unit,
    onMarkerAnnotationClick: (Point, Double?) -> Unit,
    onMarkerAnnotationLongPress: (Point, Double?) -> Unit,

    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {},
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit = {},
    onSiteElevation: (Int, Double) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        MapView(
            center                           = coordinates,
            newMarker                        = newMarker,
            newMarkerStatus                  = newMarkerStatus,
            launchSites                      = launchSites,
            mapViewportState                 = mapViewportState,
            modifier                         = Modifier.fillMaxSize(),
            showAnnotations                  = showAnnotations,

            // now passing through the two-arg lambdas
            onMapLongClick                   = onMapLongClick,
            onMarkerAnnotationClick          = onMarkerAnnotationClick,
            onMarkerAnnotationLongPress      = onMarkerAnnotationLongPress,

            onLaunchSiteMarkerClick          = onLaunchSiteMarkerClick,
            onSavedMarkerAnnotationLongPress = onSavedMarkerAnnotationLongPress,
            onSiteElevation                  = onSiteElevation
        )
    }
}

