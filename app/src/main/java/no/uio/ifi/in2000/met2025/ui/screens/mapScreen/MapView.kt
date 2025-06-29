/*
 * This file defines a Composable MapView for displaying launch sites and simulated rocket trajectories using Mapbox.
 * Main functionality:
 *  - Render an interactive Mapbox map with markers for saved launch sites and a user-added marker.
 *  - Fetch and display terrain elevations via Mapbox DEM.
 *  - Draw and animate a 3D model trajectory of a rocket flight.
 * Special notes:
 *  - Expects trajectoryPoints as a list of (RealVector lat/lon/alt, unused, RocketState).
 *  - Uses MapView.getElevation() for DEM retrieval; requires a short delay for terrain loading.
 */

package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.modelLayer
import com.mapbox.maps.extension.style.layers.generated.skyLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ModelType
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.layers.properties.generated.SkyType
import com.mapbox.maps.extension.style.model.addModel
import com.mapbox.maps.extension.style.model.model
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.domain.RocketState
import org.apache.commons.math3.linear.RealVector
import com.mapbox.maps.extension.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlin.math.sqrt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.mapbox.maps.extension.style.sources.updateImage

/**
 * Displays a Mapbox map with:
 *  - Centered camera on `center` coordinates.
 *  - A "new" marker for user placement.
 *  - Saved launch site markers with elevation labels.
 *  - 3D trajectory models representing rocket states and flight.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun MapView(
    center: Pair<Double, Double>,
    newMarker: LaunchSite?,
    newMarkerStatus: Boolean,
    launchSites: List<LaunchSite>,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    showAnnotations: Boolean = true,
    onMapLongClick: (Point, Double?) -> Unit,
    onMarkerAnnotationClick: (Point, Double?) -> Unit,
    onMarkerAnnotationLongPress: (Point, Double?) -> Unit,
    onLaunchSiteMarkerClick: (LaunchSite) -> Unit = {},
    onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit = {},
    onSiteElevation: (Int, Double) -> Unit,
    trajectoryPoints: List<Triple<RealVector, Double, RocketState>>, // sim points: (lat,lon,altAboveLaunchDatum)
    isAnimating: Boolean,
    onAnimationEnd: () -> Unit,
    styleReloadTrigger: Int
) {
    val mapState = rememberMapState {
        cameraOptions {
            center(Point.fromLngLat(center.second, center.first))
            zoom(12.0); pitch(0.0); bearing(0.0)
        }
    }
    val scope = rememberCoroutineScope()
    var requestedPts by remember { mutableStateOf(setOf<Point>()) }
    var temporaryMarker: Point? by rememberSaveable { mutableStateOf(null) }
    var markerElevation: Double? by rememberSaveable { mutableStateOf(null) }
    var mapViewRef: MapView? by remember { mutableStateOf(null) }
    var baseStyleLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(styleReloadTrigger) {
        baseStyleLoaded = false
    }

    /**
     * Fetches true terrain elevation for the given point via Mapbox DEM and
     * calls onSiteElevation(siteId, elevation) to store the result.
     * Delays briefly to ensure DEM source is loaded.
     */
    suspend fun fetchTrueElevationAndStore(siteId: Int, pt: Point) {
        // Allow Mapbox terrain tiles to initialize
        delay(500)
        val dem = mapViewRef
            ?.mapboxMap
            ?.getElevation(pt)
        if (dem != null) {
            onSiteElevation(siteId, dem)
        }
        markerElevation = dem
    }

    LaunchedEffect(newMarker) {
        newMarker?.let { site ->
            temporaryMarker = Point.fromLngLat(site.longitude, site.latitude)
            markerElevation = site.elevation
            if (site.elevation == null && mapViewRef != null) {
                fetchTrueElevationAndStore(site.uid, temporaryMarker!!)
            }
        }
    }


    Box(modifier.fillMaxSize()) {
        //key(styleReloadTrigger) {
            MapboxMap(
                modifier             = modifier.fillMaxSize(),
                mapState             = mapState,
                mapViewportState     = mapViewportState,
                onMapLongClickListener = { pt -> onMapLongClick(pt, null); true },
                style                = { /* no-op: we load style imperatively below */ }
            ) {
                // Base style + DEM + terrain + sky + globe
                MapEffect(mapViewRef, styleReloadTrigger) { mv ->
                    mapViewRef = mv
                    if (!baseStyleLoaded) {
                        baseStyleLoaded = true
                        mv.mapboxMap.loadStyle(styleExtension = style(Style.SATELLITE_STREETS) {
                            +rasterDemSource("dem") {
                                url("mapbox://mapbox.mapbox-terrain-dem-v1"); tileSize(
                                512
                            )
                            }
                            +terrain("dem") { exaggeration(1.0) }
                            +skyLayer("sky") { skyType(SkyType.ATMOSPHERE) }
                            +projection(ProjectionName.GLOBE)
                        }) {
                            (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                                enabled = true
                                locationPuck = createDefault2DPuck(withBearing = true)
                                puckBearing = PuckBearing.COURSE
                                puckBearingEnabled = true
                            }
                        }
                    }
                }

                // Remove existing trajectory and endpoint circles when no points
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) {
                        mv.mapboxMap.getStyle { style ->
                            // Remove all "traj-lyr-*" layers
                            style.styleLayers
                                .map { it.id }
                                .filter { it.startsWith("traj-lyr-") }
                                .forEach { style.removeStyleLayer(it) }

                            // Remove all "traj-src-*" sources
                            style.styleSources
                                .map { it.id }
                                .filter { it.startsWith("traj-src-") }
                                .forEach { style.removeStyleSource(it) }

                            // Also remove our endpoint circles
                            style.removeStyleLayer("endpoint-lyr-start")
                            style.removeStyleLayer("endpoint-lyr-end")
                            style.removeStyleSource("endpoint-src-start")
                            style.removeStyleSource("endpoint-src-end")
                        }
                    }
                }
                // Draw trajectory points as 3D models
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) return@MapEffect

                    mv.mapboxMap.getStyle { style ->
                        // Remove all "traj-lyr-*" layers
                        style.styleLayers
                            .map { it.id }
                            .filter { it.startsWith("traj-lyr-") }
                            .forEach { style.removeStyleLayer(it) }

                        // Remove all "traj-src-*" sources
                        style.styleSources
                            .map { it.id }
                            .filter { it.startsWith("traj-src-") }
                            .forEach { style.removeStyleSource(it) }

                        // Also remove our endpoint circles
                        style.removeStyleLayer("endpoint-lyr-start")
                        style.removeStyleLayer("endpoint-lyr-end")
                        style.removeStyleSource("endpoint-src-start")
                        style.removeStyleSource("endpoint-src-end")
                    }

                    // Offset model rendering
                    val firstFreeFlightIdx = trajectoryPoints
                        .indexOfFirst { it.third == RocketState.FREE_FLIGHT }
                        .takeIf { it >= 0 } ?: 0
                    val rocketOffset    = 5
                    val rocketModelIdx  = firstFreeFlightIdx + rocketOffset

                    val parachuteOffset   = 1 
                    val firstParachuteIdx = trajectoryPoints
                        .indexOfFirst { it.third == RocketState.PARACHUTE_DEPLOYED }
                        .takeIf { it >= 0 }
                    val parachuteModelIdx = firstParachuteIdx?.plus(parachuteOffset)

                    // Fallback terrain elevation at launch
                    val launchElev = trajectoryPoints.first().first.getEntry(2)

                    // Draw trajectory points
                    trajectoryPoints.forEachIndexed { idx, (vec, _, state) ->
                        // Limit amount of parachute points
                        if (state == RocketState.PARACHUTE_DEPLOYED
                            && idx != parachuteModelIdx
                            && idx % 10 != 0) return@forEachIndexed

                        val lat    = vec.getEntry(0)
                        val lon    = vec.getEntry(1)
                        val absAlt = vec.getEntry(2)
                        val terrain = mv.mapboxMap
                            .getElevation(Point.fromLngLat(lon, lat))
                            ?: launchElev
                        val relAlt = absAlt - terrain

                        val modelUri = when {
                            idx == rocketModelIdx -> "asset://Rocket.glb"
                            parachuteModelIdx != null && idx == parachuteModelIdx -> "asset://parachute_offset.glb"
                            else -> when (state) {
                                RocketState.ON_LAUNCH_RAIL     -> "asset://PurpleIso.glb"
                                RocketState.THRUSTING          -> "asset://RedIso.glb"
                                RocketState.FREE_FLIGHT        -> "asset://OrangeIso.glb"
                                RocketState.PARACHUTE_DEPLOYED -> "asset://LightBlueIso.glb"
                                RocketState.LANDED             -> "asset://GreenIso.glb"
                            }
                        }

//                         Decide scale for rocket and parachute
                        val scaleVec = when {
                            idx == rocketModelIdx -> listOf(200.0, 200.0, 200.0)
                            parachuteModelIdx != null && idx == parachuteModelIdx -> listOf(60.0, 60.0, 60.0)
                            else -> listOf(5.0, 5.0, 5.0)
                        }

                        //// Dynamic scaling for ISO models: first 5 grow 1→5, last 5 shrink 5→1, middle stay 5//val dynSize = when {//    idx < 5                -> (idx + 1).toDouble()           // 0→1, …, 4→5//    idx >= totalPts - 5    -> (totalPts - idx).toDouble()    // (total-5)→5, …, (total-1)→1//    else                   -> 5.0//}//val scaleVec = when {//    idx == rocketModelIdx                                   ->//        listOf(200.0, 200.0, 200.0)                         // rocket fixed scale//    parachuteModelIdx != null && idx == parachuteModelIdx  ->//        listOf(60.0, 60.0, 60.0)                            // parachute fixed scale//    else                                                    ->//        listOf(dynSize, dynSize, dynSize)                  // ISO models dynamic scale//}
//                        val totalPts = trajectoryPoints.size
//                        val dynSize = when {
//                            idx < 5 -> (idx + 1).toDouble()
//                            idx >= totalPts - 5 -> (totalPts - idx).toDouble()
//                            else -> 5.0
//                        }
//                        val scaleVec = when {
//                            idx == rocketModelIdx -> listOf(200.0, 200.0, 200.0)
//                            parachuteModelIdx != null && idx == parachuteModelIdx -> listOf(60.0, 60.0, 60.0)
//                            else -> listOf(dynSize, dynSize, dynSize)
//                        }

                        val modelId  = "traj-model-$idx"
                        val sourceId = "traj-src-$idx"
                        val layerId  = "traj-lyr-$idx"

                        mv.mapboxMap.getStyle { style ->
                            if (!style.styleLayers.any { it.id == modelId }) {
                                mv.mapboxMap.addModel(model(modelId) {
                                    uri(modelUri)
                                })
                                println("➤ added MODEL $modelId → $modelUri")
                            }
                            if (!style.styleSources.any { it.id == sourceId }) {
                                style.addSource(geoJsonSource(sourceId) {
                                    data(FeatureCollection.fromFeatures(arrayOf(
                                        Feature.fromGeometry(Point.fromLngLat(lon, lat, relAlt))
                                    )).toJson())
                                })
                            }
                            if (!style.styleLayers.any { it.id == layerId }) {
                                style.addLayer(modelLayer(layerId, sourceId) {
                                    modelId(modelId)
                                    modelType(ModelType.COMMON_3D)
                                    modelScale(scaleVec)
                                    modelTranslation(listOf(0.0, 0.0, relAlt))
                                    modelCastShadows(true)
                                    modelReceiveShadows(true)
                                })
                                if (style.styleLayers.any { it.id == layerId }) {
                                    println("ModelLayer $layerId added successfully.")
                                } else {
                                    println("Failed to add ModelLayer $layerId.")
                                }
                            }
                        }
                    }
                }

                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.size >= 2) {
                        val fv = trajectoryPoints.first().first
                        val lv = trajectoryPoints.last().first
                        val startPt = Point.fromLngLat(fv.getEntry(1), fv.getEntry(0))
                        val endPt   = Point.fromLngLat(lv.getEntry(1), lv.getEntry(0))
                        addTrajectoryEndpointsOnGround(
                            mapView      = mv,
                            start        = startPt,
                            end          = endPt,
                            radiusMeters = 500.0  // adjust as needed
                        )
                    }
                }

                // Recenter the camera on the midpoint
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) return@MapEffect

                    // grab the first and last positions
                    val firstVec = trajectoryPoints.first().first
                    val lastVec  = trajectoryPoints.last().first

                    // compute midpoint (lat, lon, alt)
                    val midLat  = (firstVec.getEntry(0) + lastVec.getEntry(0)) / 2.0
                    val midLon  = (firstVec.getEntry(1) + lastVec.getEntry(1)) / 2.0
                    val midAlt  = (firstVec.getEntry(2) + lastVec.getEntry(2)) / 2.0
                    val centerPoint = Point.fromLngLat(midLon, midLat, midAlt)

                    mv.mapboxMap.easeTo(
                        CameraOptions.Builder()
                            .center(centerPoint)
                            .pitch(80.0)
                            .zoom( 11.0 )
                            .build(),
                        MapAnimationOptions.mapAnimationOptions {
                            duration(1000L)
                        }
                    )

                    onAnimationEnd()
                }

                // MapView ref & enable location puck
                MapEffect(Unit) { mv ->
                    mapViewRef = mv
                    (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                }


                // Draw the “new” marker
                // Null check needed for first launch of app
                if (newMarkerStatus && newMarker != null) {
                    key(newMarker.uid to newMarker.name) {
                        val icon = rememberIconImage(
                            key = R.drawable.red_marker,
                            painter = painterResource(R.drawable.red_marker)
                        )
                        val pt =
                            temporaryMarker ?: Point.fromLngLat(
                                newMarker.longitude,
                                newMarker.latitude
                            )
                        PointAnnotation(point = pt) { iconImage = icon }
                        if (showAnnotations) {
                            ViewAnnotation(
                                options = viewAnnotationOptions {
                                    geometry(pt)
                                    annotationAnchor {
                                        anchor(ViewAnnotationAnchor.BOTTOM).offsetY(
                                            60.0
                                        )
                                    }
                                    allowOverlap(true)
                                }
                            ) {
                                MarkerLabel(
                                    name = newMarker.name,
                                    lat = "%.4f".format(pt.latitude()),
                                    lon = "%.4f".format(pt.longitude()),
                                    elevation = markerElevation?.let { "%.1f m".format(it) },
                                    isLoadingElevation = markerElevation == null, // Shows loader
                                    onClick = { onMarkerAnnotationClick(pt, markerElevation) },
                                    onLongPress = {
                                        onMarkerAnnotationLongPress(
                                            pt,
                                            markerElevation
                                        )
                                    },
                                    onDoubleClick = {
                                        scope.launch {
                                            mapViewportState.easeTo(
                                                cameraOptions {
                                                    center(pt)
                                                    zoom(14.0)
                                                    pitch(0.0)
                                                    bearing(0.0)
                                                },
                                                MapAnimationOptions.mapAnimationOptions {
                                                    duration(
                                                        1000L
                                                    )
                                                }
                                            )
                                        }
                                        onLaunchSiteMarkerClick(newMarker)
                                    }
                                )

                            }
                        }
                    }
                }

                // Draw all other launch sites
                launchSites
                    .filter { it.name !in listOf("Last Visited", "New Marker") }
                    .forEach { site ->
                        val sitePoint = Point.fromLngLat(site.longitude, site.latitude)
                        val siteImage = rememberIconImage(
                            key = "launchSite_${site.uid}",
                            painter = painterResource(R.drawable.red_marker)
                        )

                        PointAnnotation(point = sitePoint) { iconImage = siteImage }

                        if (showAnnotations) {
                            key(site.uid to site.name) {
                                ViewAnnotation(
                                    options = viewAnnotationOptions {
                                        geometry(sitePoint)
                                        annotationAnchor {
                                            anchor(ViewAnnotationAnchor.BOTTOM).offsetY(
                                                60.0
                                            )
                                        }
                                        allowOverlap(true)
                                    }
                                ) {
                                    MarkerLabel(
                                        name = site.name,
                                        lat = "%.4f".format(site.latitude),
                                        lon = "%.4f".format(site.longitude),
                                        elevation = site.elevation?.let { "%.1f m".format(it) }
                                            ?: "—",
                                        onClick = { /* tap‐noop */ },
                                        onDoubleClick = {
                                            scope.launch {
                                                mapViewportState.easeTo(
                                                    cameraOptions {
                                                        center(sitePoint)
                                                        zoom(14.0)
                                                        pitch(0.0)
                                                        bearing(0.0)
                                                    },
                                                    MapAnimationOptions.mapAnimationOptions {
                                                        duration(
                                                            1000L
                                                        )
                                                    }
                                                )
                                            }
                                            onLaunchSiteMarkerClick(site)
                                        },
                                        onLongPress = { onSavedMarkerAnnotationLongPress(site) }
                                    )
                                }
                            }
                        }

                        LaunchedEffect(site.uid, site.elevation) {
                            if (site.elevation == null
                                && !requestedPts.contains(sitePoint)
                                && mapViewRef != null
                            ) {
                                requestedPts = requestedPts + sitePoint
                                val dem = mapViewRef!!
                                    .mapboxMap
                                    .getElevation(sitePoint)
                                dem?.let { onSiteElevation(site.uid, it) }
                            }
                        }
                    }
        }
    }
}

fun addTrajectoryEndpointsOnGround(
    mapView: MapView,
    start: Point,
    end: Point,
    radiusMeters: Double,
    bitmapSizePx: Int = 512
) {
    mapView.mapboxMap.getStyle { style ->

        //  both bitmaps: fade+outline
        val startBmp = createFadeCircleWithOutlineBitmap(
            size = bitmapSizePx / 2,
            innerColor    = android.graphics.Color.YELLOW,
            outerColor    = android.graphics.Color.TRANSPARENT,
            outlineColor  = android.graphics.Color.YELLOW,
            outlineWidthPx = 8f
        )
        val endBmp = createFadeCircleWithOutlineBitmap(
            size = bitmapSizePx,
            innerColor    = android.graphics.Color.parseColor("#ADD8E6"), // light blue
            outerColor    = android.graphics.Color.TRANSPARENT,
            outlineColor  = android.graphics.Color.parseColor("#ADD8E6"),
            outlineWidthPx = 8f
        )

        fun addCircle(center: Point, suffix: String, bmp: Bitmap, circleRadius: Double) {
            val srcId = "endpoint-src-$suffix"
            val lyrId = "endpoint-lyr-$suffix"

            // cleanup
            style.removeStyleLayer(lyrId)
            style.removeStyleSource(srcId)

            // compute ground corners
            val diag = circleRadius * sqrt(2.0)
            val bearings = listOf(315.0, 45.0, 135.0, 225.0)
            val corners = bearings.map { brg ->
                TurfMeasurement.destination(center, diag, brg, TurfConstants.UNIT_METERS)
            }
            val coords = corners.map { listOf(it.longitude(), it.latitude()) }

            // create & add ImageSource
            val imageSource = ImageSource.Builder(srcId)
                .coordinates(coords)
                .build()
            style.addSource(imageSource)
            imageSource.updateImage(bmp)

            // add RasterLayer
            style.addLayer(
                RasterLayer(lyrId, srcId)
                    .rasterOpacity(1.0)
            )
        }

        // place start (half‐size yellow) and end (full‐size light‐blue)
        addCircle(start, "start", startBmp, radiusMeters * 0.5)
        addCircle(end,   "end",   endBmp,   radiusMeters)
    }
}

/** fade center→edge plus a stroke */
private fun createFadeCircleWithOutlineBitmap(
    size: Int,
    innerColor: Int,
    outerColor: Int,
    outlineColor: Int,
    outlineWidthPx: Float
): Bitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val r = size / 2f

    // fill with radial gradient
    val shader = RadialGradient(
        r, r, r,
        innerColor, outerColor,
        Shader.TileMode.CLAMP
    )
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
    canvas.drawCircle(r, r, r, fillPaint)

    // draw solid outline
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = outlineColor
        strokeWidth = outlineWidthPx
    }
    // inset by half the stroke so it draws crisply inside the bitmap
    canvas.drawCircle(r, r, r - outlineWidthPx/2, strokePaint)

    return bmp
}