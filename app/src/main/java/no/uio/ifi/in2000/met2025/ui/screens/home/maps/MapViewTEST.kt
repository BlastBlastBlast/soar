package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterDemSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.modelLayer
import com.mapbox.maps.extension.style.layers.generated.skyLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.LineTranslateAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.ModelType
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.layers.properties.generated.SkyType
import com.mapbox.maps.extension.style.model.addModel
import com.mapbox.maps.extension.style.model.model
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
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
import no.uio.ifi.in2000.met2025.ui.screens.home.calculateBearing
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import com.mapbox.maps.extension.style.expressions.dsl.generated.array
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.dsl.generated.toNumber

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
    trajectoryPoints: List<Pair<RealVector, Double>>, // sim points: (lat,lon,altAboveLaunchDatum)
    isAnimating: Boolean,
    onAnimationEnd: () -> Unit
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
    val rasterDemSourceState = rememberRasterDemSourceState().apply {
        url = StringValue("mapbox://mapbox.mapbox-terrain-dem-v1")
        tileSize = LongValue(512)
    }
    var mapViewRef: MapView? by remember { mutableStateOf(null) }
    var styleLoaded by remember { mutableStateOf(false) }
    var worldTrajectory by remember { mutableStateOf<List<Pair<RealVector, Double>>>(emptyList()) }
    val MODEL_ID = "redball"
    val SRC_ID = "redball-src"
    val LAYER_ID = "redball-layer"
    val DEM_SOURCE_ID = "dem-source"
    val DEM_URL = "mapbox://mapbox.mapbox-terrain-dem-v1"

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

    // 1️⃣ Build worldTrajectory only once, mapping (lon, lat, simAlt) → lift above ground
    LaunchedEffect(trajectoryPoints, mapViewRef) {
        val mv = mapViewRef ?: return@LaunchedEffect
        worldTrajectory = trajectoryPoints.map { (vec, simAlt) ->
            val lon = vec.getEntry(0)
            val lat = vec.getEntry(1)
            // simAlt is altitude above launch datum; now fetch ground elevation
            val ground = mv.mapboxMap.getElevation(Point.fromLngLat(lon, lat)) ?: 0.0
            // final 3D point = [lon, lat, ground + simAlt]
            ArrayRealVector(doubleArrayOf(lon, lat, ground + simAlt)) to simAlt
        }
    }

    LaunchedEffect(trajectoryPoints, mapViewRef) {
        val mv = mapViewRef ?: return@LaunchedEffect
        worldTrajectory = trajectoryPoints.map { (vec, simAlt) ->
            val lon = vec.getEntry(0)
            val lat = vec.getEntry(1)
            // fetch ground elevation under each point
            val ground = mv.mapboxMap.getElevation(Point.fromLngLat(lon,lat)) ?: 0.0
            // store (lon,lat,ground+simAlt) → simAlt
            ArrayRealVector(doubleArrayOf(lon, lat, ground + simAlt)) to simAlt
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapState = rememberMapState { cameraOptions { center(Point.fromLngLat(center.second, center.first)); zoom(12.0) } },
            mapViewportState = rememberMapViewportState(),
            style = { /* no-op */ }
        ) {
            // 2️⃣ Load style, register model, and add your ModelLayer exactly once
            MapEffect(mapViewRef) { mv ->
                mapViewRef = mv
                mv.mapboxMap.loadStyle(
                    styleExtension = style(Style.SATELLITE_STREETS) {
                        +rasterDemSource("dem") { url("mapbox://mapbox.mapbox-terrain-dem-v1"); tileSize(512) }
                        +terrain("dem") { exaggeration(1.0) }
                        +skyLayer("sky") { skyType(SkyType.ATMOSPHERE); skyAtmosphereSun(listOf(-50.0,90.2)) }
                        +projection(ProjectionName.GLOBE)
                    }
                ) {
                    // register your GLB
                    mv.mapboxMap.addModel(model("redball") { uri("asset://redball.glb") })

                    // now create source + ModelLayer (with translation driven by each feature’s "altitude" prop)
                    mv.mapboxMap.getStyle { style ->
                        if (style.getSource("traj-src") == null) {
                            style.addSource(
                                geoJsonSource("traj-src") {
                                    data(FeatureCollection.fromFeatures(emptyArray()).toJson())
                                }
                            )
                        }
                        if (style.getLayer("traj-layer") == null) {
                            style.addLayer(
                                modelLayer("traj-layer","traj-src") {
                                    modelId("redball")
                                    modelType(ModelType.COMMON_3D)
                                    modelScale(listOf(2.0,2.0,2.0))
                                    // THIS is the key: translate up by each feature’s "altitude"
                                    // ← This is the correct syntax:
                                    modelTranslation (
                                        array {
                                            literal(0.0)                // X offset
                                            literal(0.0)                // Y offset
                                            toNumber {                   // Z offset
                                                get("altitude")           // ← builder lambda
                                            }
                                        }
                                    )
                                    modelCastShadows(true)
                                    modelReceiveShadows(true)
                                }
                            )
                        }
                    }
                }
            }

            // 3️⃣ Push your lifted points into that source on every change
            MapEffect(worldTrajectory) { mv ->
                mv.mapboxMap.getStyle { style ->
                    val feats = worldTrajectory.map { (vec,_) ->
                        Feature.fromGeometry(
                            Point.fromLngLat(vec.getEntry(0), vec.getEntry(1), vec.getEntry(2))
                        ).apply {
                            addNumberProperty("altitude", vec.getEntry(2))
                        }
                    }.toTypedArray()
                    style.getSourceAs<GeoJsonSource>("traj-src")
                        ?.featureCollection(FeatureCollection.fromFeatures(feats))
                }
            }

            // 4️⃣ Animate the camera along the same lifted path
            MapEffect(worldTrajectory to isAnimating) { mv ->
                if (isAnimating && worldTrajectory.isNotEmpty()) {
                    (mv.context as ComponentActivity).lifecycleScope.launch {
                        var prev: Point? = null
                        for ((vec,_) in worldTrajectory) {
                            val lon = vec.getEntry(0)
                            val lat = vec.getEntry(1)
                            val bearing = prev?.let {
                                calculateBearing(it.longitude(), it.latitude(), lon, lat)
                            } ?: 0.0
                            mv.mapboxMap.easeTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(lon,lat))
                                    .pitch(60.0).bearing(bearing).zoom(14.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions { duration(200L) }
                            )
                            prev = Point.fromLngLat(lon,lat)
                        }
                        onAnimationEnd()
                    }
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
            if (newMarkerStatus) {
                val icon = rememberIconImage(
                    key = R.drawable.red_marker,
                    painter = painterResource(R.drawable.red_marker)
                )
                val pt =
                    temporaryMarker ?: Point.fromLngLat(newMarker!!.longitude, newMarker.latitude)
                PointAnnotation(point = pt) { iconImage = icon }
                if (showAnnotations) {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            geometry(pt)
                            annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM).offsetY(60.0) }
                            allowOverlap(true)
                        }
                    ) {
                        MarkerLabel(
                            name = newMarker?.name ?: "New Marker",
                            lat = "%.4f".format(pt.latitude()),
                            lon = "%.4f".format(pt.longitude()),
                            elevation = markerElevation?.let { "%.1f m".format(it) },
                            isLoadingElevation = markerElevation == null,   // show spinner when null
                            onClick = { onMarkerAnnotationClick(pt, markerElevation) },
                            onLongPress = { onMarkerAnnotationLongPress(pt, markerElevation) },
                            onDoubleClick = { /* no-op */ }
                        )

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
                                elevation = site.elevation?.let { "%.1f m".format(it) } ?: "—",
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
                                            MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                                        )
                                    }
                                    onLaunchSiteMarkerClick(site)
                                },
                                onLongPress = { onSavedMarkerAnnotationLongPress(site) }
                            )
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

            /*MapEffect(worldTrajectory) { mv ->
                mv.mapboxMap.getStyle { style ->
                    // a) create the source once if missing
                    if (style.getSource(TRAJ_SOURCE) == null) {
                        style.addSource(geoJsonSource(TRAJ_SOURCE) {
                            data(FeatureCollection.fromFeatures(emptyArray()).toJson())
                        })
                    }
                    // b) push your *new* FeatureCollection into that source
                    val fc = FeatureCollection.fromFeatures(
                        worldTrajectory.map { (vec, _) ->
                            Feature.fromGeometry(
                                Point.fromLngLat(vec.getEntry(1), vec.getEntry(0), vec.getEntry(2))
                            )
                        }.toTypedArray()
                    )
                    style.getSourceAs<GeoJsonSource>(TRAJ_SOURCE)
                        ?.featureCollection(fc)

                    // c) add ModelLayer once if missing (it will read from the updated source)
                    if (style.getLayer(TRAJ_LAYER) == null) {
                        style.addLayer(modelLayer(TRAJ_LAYER, TRAJ_SOURCE) {
                            modelId(MODEL_ID)
                            modelType(ModelType.COMMON_3D)
                            modelScale(listOf(2.0, 2.0, 2.0))
                            modelCastShadows(true)
                            modelReceiveShadows(true)
                        })
                    }
                }
            }

            MapEffect(key1 = trajectoryPoints) { mv ->
                mv.mapboxMap.getStyle { style ->
                    if (style.getSource("traj-src") == null) {
                        style.addSource(
                            geoJsonSource("traj-src") { lineMetrics(true) }
                        )
                        style.addLayer(
                            lineLayer("traj-layer", "traj-src") {
                                lineColor("#FF0000")
                                lineWidth(4.0)
                                lineTranslateAnchor(LineTranslateAnchor.MAP)
                            }
                        )
                    }
                    if (trajectoryPoints.isNotEmpty()) {
                        val pts = trajectoryPoints.map { (vec, speed) ->
                            // now we read the vector as [lon, lat, alt]
                            Point.fromLngLat(
                                vec.getEntry(0),
                                vec.getEntry(1),
                                vec.getEntry(2)
                            )
                        }
                        style.getSourceAs<GeoJsonSource>("traj-src")
                            ?.featureCollection(
                                FeatureCollection.fromFeatures(
                                    arrayOf(Feature.fromGeometry(LineString.fromLngLats(pts)))
                                )
                            )
                    }
                }
            }

            // ✦ animate the camera:
            MapEffect(key1 = trajectoryPoints, key2 = isAnimating) { mv ->
                if (isAnimating && trajectoryPoints.isNotEmpty()) {
                    (mv.context as ComponentActivity).lifecycleScope.launch {
                        var prev: Point? = null
                        for ((vec, speed) in trajectoryPoints) {
                            val lon = vec.getEntry(0)
                            val lat = vec.getEntry(1)
                            val bearing = prev?.let {
                                calculateBearing(
                                    it.longitude(), it.latitude(),
                                    lon, lat
                                )
                            } ?: 0.0
                            mv.mapboxMap.easeTo(
                                CameraOptions.Builder()
                                    .center(Point.fromLngLat(lon, lat))
                                    .pitch(60.0)
                                    .bearing(bearing)
                                    .zoom(14.0)
                                    .build(),
                                MapAnimationOptions.mapAnimationOptions { duration(200L) }
                            )
                            prev = Point.fromLngLat(lon, lat)
                        }
                        onAnimationEnd()
                    }
                }
            }*/
        }
    }
}

