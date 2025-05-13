package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
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


// FIXME: CHECK NULL POINTER EXCEPTION WHEN PLACING FIRST MARKER
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
    // Trajectory integration
    trajectoryPoints: List<Triple<RealVector, Double, RocketState>>, // sim points: (lat,lon,altAboveLaunchDatum)
    isAnimating: Boolean,
    onAnimationEnd: () -> Unit,
    styleReloadTrigger: Int
) {
    // 1) Map state & scope
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

    // ← NEW: helper to fetch & store DEM elevation
    suspend fun fetchTrueElevationAndStore(siteId: Int, pt: Point) {
        // give Mapbox a frame to load terrain
        delay(500)
        val dem = mapViewRef
            ?.mapboxMap
            ?.getElevation(pt)      // returns Double?
        if (dem != null) {
            onSiteElevation(siteId, dem)
        }
        markerElevation = dem
    }

    // 3) Sync VM-driven newMarker into temporaryMarker
    LaunchedEffect(newMarker) {
        newMarker?.let { site ->
            temporaryMarker = Point.fromLngLat(site.longitude, site.latitude)
            markerElevation = site.elevation  // might be null
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
                // ─── Base style + DEM + terrain + sky + globe ───────────────

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



                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) {
                        mv.mapboxMap.getStyle { style ->
                            // 1) remove all "traj-lyr-*" layers
                            style.styleLayers
                                .map { it.id }                              // StyleObjectInfo.id
                                .filter { it.startsWith("traj-lyr-") }
                                .forEach { layerId ->
                                    style.removeStyleLayer(layerId)           // removeStyleLayer(String)
                                }

                            // 2) then remove all "traj-src-*" sources
                            style.styleSources
                                .map { it.id }
                                .filter { it.startsWith("traj-src-") }
                                .forEach { sourceId ->
                                    style.removeStyleSource(sourceId)         // removeStyleSource(String)
                                }
                        }
                    }
                }

                // ─── 3) When trajectoryPoints appear: register model+source+layer per point ───
                MapEffect(trajectoryPoints) { mv ->
                    if (trajectoryPoints.isEmpty()) return@MapEffect
//                mv.mapboxMap.getStyle { style ->
//                    trajectoryPoints.forEachIndexed { idx, (vec, _, state) ->
//                        if (state == RocketState.PARACHUTE_DEPLOYED && idx%10 != 0) return@forEachIndexed
//
//                        val lon = vec.getEntry(1)
//                        val lat = vec.getEntry(0)
//                        val alt = vec.getEntry(2)
//
//                        val modelId  = "redball-$idx"
//                        val sourceId = "traj-src-$idx"
//                        val layerId  = "traj-lyr-$idx"
//
//
//                        // 3.1) add the GLB asset once
//                        if (style.getLayer(modelId) == null) {
//                            mv.mapboxMap.addModel(model(modelId) {
//                                uri("asset://tiny_icosphere2.glb")
//                            })
//                        }
//
//                        // 3.2) add a GeoJSON source at exactly (lon,lat,alt)
//                        if (style.getSource(sourceId) == null) {
//                            style.addSource(geoJsonSource(sourceId) {
//                                data(
//                                    FeatureCollection.fromFeatures(arrayOf(
//                                        Feature.fromGeometry(Point.fromLngLat(lon, lat, alt))
//                                    )).toJson()
//                                )
//                            })
//                        }
//
//                        // 3.3) add a ModelLayer, shifted up by altitude
//                        if (style.getLayer(layerId) == null) {
//                            style.addLayer(modelLayer(layerId, sourceId) {
//                                modelId(modelId)
//                                modelType(ModelType.COMMON_3D)
//                                modelScale(listOf(5.0, 5.0, 5.0))
//                                modelTranslation(listOf(0.0, 0.0, alt))
//                                modelCastShadows(true)
//                                modelReceiveShadows(true)
//                            })
//                        }
//                    }
//                }
                    val launchElev = trajectoryPoints.first().first.getEntry(2)

                    trajectoryPoints.forEachIndexed { idx, (vec, _, state) ->
                        if (state == RocketState.PARACHUTE_DEPLOYED && idx % 10 != 0) return@forEachIndexed

                        // 2) sim coords
                        val lat    = vec.getEntry(0)
                        val lon    = vec.getEntry(1)
                        val absAlt = vec.getEntry(2)        // ASL altitude from your sim

                        // 3) query the DEM at this (lon,lat)
                        val pt2d    = Point.fromLngLat(lon, lat)
                        val terrain = mv.mapboxMap.getElevation(pt2d) ?: launchElev

                        // 4) compute metres _above_ that terrain
                        val relAlt  = absAlt - terrain

                        val modelId = "redball-$idx"
                        val sourceId = "traj-src-$idx"
                        val layerId = "traj-lyr-$idx"

                        mv.mapboxMap.getStyle { style ->
                            // Add the GLB model if not already added
                            if (!style.styleLayers.any { it.id == modelId }) {
                                style.addModel(model(modelId) {
                                    uri("asset://tiny_icosphere2.glb")
                                })
                            }

                            // Add the GeoJSON source if not already added
                            if (!style.styleSources.any { it.id == sourceId }) {
                                style.addSource(geoJsonSource(sourceId) {
                                    data(
                                        FeatureCollection.fromFeatures(
                                            listOf(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        lon,
                                                        lat,
                                                        relAlt
                                                    )
                                                )
                                            )
                                        ).toJson()
                                    )
                                })
                            }

                            // Add the ModelLayer if not already added
                            if (!style.styleLayers.any { it.id == layerId }) {
                                style.addLayer(modelLayer(layerId, sourceId) {
                                    modelId(modelId)
                                    modelType(ModelType.COMMON_3D)
                                    modelScale(listOf(5.0, 5.0, 5.0))
                                    modelTranslation(listOf(0.0, 0.0, relAlt))
                                    modelCastShadows(true)
                                    modelReceiveShadows(true)
                                })
                                // Log layer addition
                                if (style.styleLayers.any { it.id == layerId }) {
                                    println("ModelLayer $layerId added successfully.")
                                } else {
                                    println("Failed to add ModelLayer $layerId.")
                                }
                            }
                        }
                    }
                }

                // ─── 4) Animate camera over the lifted trajectory ────────────────────
                MapEffect(trajectoryPoints to isAnimating) { mv ->
                    if (!isAnimating || trajectoryPoints.isEmpty()) return@MapEffect
                    (mv.context as ComponentActivity).lifecycleScope.launch {
                        var prev: Point? = null
                        trajectoryPoints.forEach { (vec, _) ->
                            val lon = vec.getEntry(1)
                            val lat = vec.getEntry(0)
                            val alt = vec.getEntry(2)
                            val p = Point.fromLngLat(lon, lat, alt)
                            val bearing = prev?.let {
                                calculateBearing(it.longitude(), it.latitude(), lon, lat)
                            } ?: 0.0
                            mv.mapboxMap.easeTo(
                                CameraOptions.Builder()
                                    .center(p)
                                    .pitch(70.0)
                                    .bearing(bearing)
                                    .zoom(12.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions { duration(200L) }
                            )
                            prev = p
                        }
                        onAnimationEnd()
                    }
                }

                // 4) (unchanged) capture MapView ref & enable location puck
                MapEffect(Unit) { mv ->
                    mapViewRef = mv
                    (mv.getPlugin("location") as? LocationComponentPlugin)?.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                }


                // 6) Draw the “new” marker
                //Null check needed for first launch of app
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
                                    isLoadingElevation = markerElevation == null,   // show spinner when null
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

                // 7) Draw all other launch sites
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

                        // only fetch terrain elevation once, and only if it's still null
                        LaunchedEffect(site.uid, site.elevation) {
                            if (site.elevation == null
                                && !requestedPts.contains(sitePoint)
                                && mapViewRef != null
                            ) {
                                // mark as requested so we don't repeat
                                requestedPts = requestedPts + sitePoint

                                // synchronous elevation query
                                val dem = mapViewRef!!
                                    .mapboxMap
                                    .getElevation(sitePoint)   // returns Double?
                                dem?.let { onSiteElevation(site.uid, it) }
                            }
                        }
                    }
            }
        }
    }
//}

/** Updated Helper */
fun idToBitmap(context: Context, @DrawableRes id: Int): Bitmap {
    val dr = ResourcesCompat.getDrawable(context.resources, id, null)
        ?: error("Drawable $id not found")
    val bmp = Bitmap.createBitmap(dr.intrinsicWidth, dr.intrinsicHeight, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(bmp).apply {
        dr.setBounds(0, 0, width, height)
        dr.draw(this)
    }
    return bmp
}
