package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.MapView
import org.apache.commons.math3.linear.RealVector

@Composable
fun MapContainer(
    coordinates: Pair<Double, Double>,
    newMarker: LaunchSite?,
    newMarkerStatus: Boolean,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,

    // ← allow nullable elevation here
    onMapLongClick: (Point, Double?) -> Unit,
    onMarkerAnnotationClick: (Point, Double?) -> Unit,
    onMarkerAnnotationLongPress: (Point, Double?) -> Unit,

    onLaunchSiteMarkerClick: (LaunchSite) -> Unit,
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit,

    // this stays non-null, because once you fetch you always pass a real Double
    onSiteElevation: (Int, Double) -> Unit,

    trajectoryPoints: List<Pair<RealVector, Double>> = emptyList(),
    isAnimating: Boolean = false,
    onAnimationEnd: () -> Unit = {}
) {
    Box(modifier.fillMaxSize()) {
        MapView(
            center                           = coordinates,
            newMarker                        = newMarker,
            newMarkerStatus                  = newMarkerStatus,
            launchSites                      = launchSites,
            mapViewportState                 = mapViewportState,
            showAnnotations                  = showAnnotations,

            // ← propagate the nullable signature
            onMapLongClick                   = onMapLongClick,
            onMarkerAnnotationClick          = onMarkerAnnotationClick,
            onMarkerAnnotationLongPress      = onMarkerAnnotationLongPress,

            onLaunchSiteMarkerClick          = onLaunchSiteMarkerClick,
            onSavedMarkerAnnotationLongPress = onSavedMarkerAnnotationLongPress,
            onSiteElevation                  = onSiteElevation,

            trajectoryPoints                 = trajectoryPoints,
            isAnimating                      = isAnimating,
            onAnimationEnd                   = onAnimationEnd,
            modifier                         = Modifier.fillMaxSize()
        )
    }
}
