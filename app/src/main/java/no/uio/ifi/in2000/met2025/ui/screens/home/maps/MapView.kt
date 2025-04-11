package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
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
    name: String,
    lat: String,
    lon: String,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: () -> Unit,
    fontSize: TextUnit = 8.sp
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.Black,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)

        ) {
            Text(
                text = name,
                fontSize = fontSize,
                color = Color.White,
                textDecoration = TextDecoration.Underline,
            )
            Row {
                Text(
                    text = "Lat: ",
                    fontSize = fontSize,
                    color = Color.White
                )
                Text(
                    text = lat,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Row {
                Text(
                    text = "Lon: ",
                    fontSize = fontSize,
                    color = Color.White
                )
                Text(
                    text = lon,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun MapView(
    center: Pair<Double, Double>,
    temporaryMarker: Point? = null,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point) -> Unit,
    onMarkerAnnotationClick: (Point) -> Unit,
    onMarkerAnnotationLongPress: (Point) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {},
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit = {}  // NEW callback
) {
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val scope = rememberCoroutineScope()

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
            // Draw the temporary marker.
            temporaryMarker?.let { point ->
                val markerImage = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = point) { iconImage = markerImage }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(point)
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            name = "New Marker",
                            lat = "%.4f".format(point.latitude()),
                            lon = "%.4f".format(point.longitude()),
                            onClick = { onMarkerAnnotationClick(point) },
                            onDoubleClick = { /* Optionally handle double tap on temporary marker */ },
                            onLongPress = { onMarkerAnnotationLongPress(point) }
                        )
                    }
                }
            }
            // Draw saved launch site markers.
            launchSites.filter { it.name != "Last Visited" }.forEach { site ->
                val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                val markerImage = rememberIconImage(
                    key = "launchSite_${site.uid}",
                    painter = painterResource(id = R.drawable.red_marker)
                )
                PointAnnotation(point = sitePoint) { iconImage = markerImage }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(sitePoint)
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM).offsetY(60.0) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            name = site.name,
                            lat = "%.4f".format(site.latitude),
                            lon = "%.4f".format(site.longitude),
                            onClick = { /* Optionally handle single tap on saved marker */ },
                            onDoubleClick = {
                                scope.launch {
                                    mapViewportState.easeTo(
                                        cameraOptions {
                                            center(sitePoint)
                                            zoom(14.0)
                                            pitch(0.0)
                                            bearing(0.0)
                                        },
                                        MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                                    )
                                }
                                onLaunchSiteMarkerClick(site)
                            },
                            onLongPress = {
                                // NEW: Instead of doing nothing, call the callback for editing saved marker.
                                onSavedMarkerAnnotationLongPress(site)
                            }
                        )
                    }
                }
            }
        }
    }
}