/*
 * Copyright 2026 adrc95 (Adrián Cabrera)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.isDarkTheme
import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.trip.advanceAlongRoute
import dev.adrc95.logronobus.core.domain.trip.drawingMayMoveTo
import dev.adrc95.logronobus.core.domain.trip.extrapolationCapMs
import dev.adrc95.logronobus.core.domain.trip.metresToDraw
import dev.adrc95.logronobus.core.domain.trip.observedPace
import dev.adrc95.logronobus.core.domain.trip.routeMetresTo
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.follow_bus
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraFit
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.include
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.DpPadding
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapLibreBusMapView(
    modifier: Modifier = Modifier,
    positions: List<BusLocationDisplayModel>,
    stops: List<StopDisplayModel> = emptyList(),
    trackedTripId: String? = null,
    trackedStopRef: String? = null,
    trackedNextStopRef: String? = null,
    onStopClick: InputAction<StopDisplayModel>?,
    onBusClick: InputAction<BusLocationDisplayModel>?,
    userLocation: LocationDisplayModel? = null,
    polylines: List<RouteLineDisplayModel> = emptyList(),
    stopEtas: Map<String, StopEtaDisplayModel> = emptyMap(),
    followBus: Boolean = false,
    routeColor: Color? = null,
) {
    val dark = isDarkTheme()
    val styleUrl = if (dark) STYLE_DARK else STYLE_LIGHT

    val routeFeatures =
        remember(polylines) {
            polylines.mapNotNull { line ->
                val points = line.points.map { Position(it.longitude, it.latitude) }
                if (points.size < 2) null else Feature(LineString(points), buildJsonObject { })
            }
        }

    val stopFeatures =
        remember(stops, stopEtas, trackedStopRef) {
            stops.mapNotNull { stop ->
                Feature(
                    geometry = Point(Position(stop.location.longitude, stop.location.latitude)),
                    properties =
                        buildJsonObject {
                            put("ref", stop.stopRef)
                            put("name", stop.name)
                            stopEtas[stop.stopRef]
                                ?.label
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { put("eta", it) }
                        },
                )
            }
        }

    val followedBus = trackedTripId?.let { id -> positions.find { it.tripDuid == id } }
    val followedNextStop = trackedNextStopRef?.let { ref -> stops.find { it.stopRef == ref } }
    val followedShape =
        remember(polylines) {
            polylines
                .firstOrNull()
                ?.points
                .orEmpty()
                .map { GeoPoint(it.latitude, it.longitude) }
        }
    val ticked =
        rememberTickedBusPosition(
            trackedTripId = trackedTripId,
            followedBus = followedBus,
            followedNextStop = followedNextStop,
            followedShape = followedShape,
            followBus = followBus,
        )

    val busFeatures =
        remember(positions, ticked, trackedTripId, followedNextStop) {
            positions.mapNotNull { bus ->
                if (bus.location.latitude == 0.0 && bus.location.longitude == 0.0) return@mapNotNull null

                val followed = bus.tripDuid == trackedTripId
                val atStop =
                    if (followed) {
                        followedNextStop?.takeIf { stop ->
                            GeoPoint(bus.location.latitude, bus.location.longitude)
                                .distanceTo(
                                    GeoPoint(stop.location.latitude, stop.location.longitude),
                                ) <= DomainConstants.AT_STOP_METRES
                        }
                    } else {
                        null
                    }
                val position =
                    when {
                        atStop != null -> {
                            Position(atStop.location.longitude, atStop.location.latitude)
                        }

                        followed -> {
                            ticked ?: Position(bus.location.longitude, bus.location.latitude)
                        }

                        else -> {
                            Position(bus.location.longitude, bus.location.latitude)
                        }
                    }
                Feature(
                    geometry = Point(position),
                    properties =
                        buildJsonObject {
                            put("ref", bus.vehicleId ?: bus.tripDuid)
                            put("line", bus.timetableId ?: "")
                            put("tracked", followed)
                            put("color", bus.lineColor?.toMapHex() ?: "#EA0046")
                            put("label", bus.labelColor.toMapHex())
                        },
                )
            }
        }

    val userFeature =
        userLocation
            ?.let {
                listOf(
                    Feature(Point(Position(it.longitude, it.latitude)), buildJsonObject { }),
                )
            }.orEmpty()

    val rememberedCamera =
        rememberSaveable(
            saver =
                rememberedCameraSaver(
                    save = { camera ->
                        cameraString(
                            camera.center.latitude,
                            camera.center.longitude,
                            camera.zoom,
                            camera.bearing,
                            camera.pitch,
                        )
                    },
                    restore = { numbers ->
                        CameraPosition(
                            center = Position(numbers[1], numbers[0]),
                            zoom = numbers[2],
                            bearing = numbers[3],
                            pitch = numbers[4],
                        )
                    },
                ),
        ) { RememberedCamera() }
    val restoredCamera = rememberedCamera.position

    val mapState =
        rememberMapState(
            baseStyle = BaseStyle.Uri(styleUrl),
            initialCameraPosition =
                restoredCamera
                    ?: CameraPosition(
                        center = Position(LOGRONO_LON, LOGRONO_LAT),
                        zoom = 13.0,
                    ),
        ) {
            val routeSource =
                rememberGeoJsonSource(GeoJsonData.Features(FeatureCollection(routeFeatures)))
            LineLayer(
                id = "route",
                source = routeSource,
                color = const(routeColor ?: Color(0xFFEA0046)),
                width = const(5.dp),
            )

            val stopSource =
                rememberGeoJsonSource(GeoJsonData.Features(FeatureCollection(stopFeatures)))

            SymbolLayer(
                id = "stops",
                source = stopSource,
                iconImage = image(STOP_ICON),
                iconSize = const(1.1f),
                iconAllowOverlap = const(true),
                hitPadding = 10.dp,
                onClick = { features ->
                    val tapped = position
                    val stop = features.toStops(stops).nearestTo(tapped)
                    if (stop != null && onStopClick != null) {
                        onStopClick(stop)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )

            SymbolLayer(
                id = "stop-etas",
                source = stopSource,
                textField = feature["eta"].asString(),
                textFont = const(listOf("Noto Sans Regular")),
                textSize = const(11.sp),
                textColor = const(Color(0xFF201A1B)),
                textHaloColor = const(Color.White),
                textHaloWidth = const(1.5.dp),
            )

            val busSource = rememberGeoJsonSource(GeoJsonData.Features(FeatureCollection(busFeatures)))
            CircleLayer(
                id = "buses",
                source = busSource,
                radius = const(8.dp),
                color = feature["color"].convertToColor(),
                strokeWidth = const(2.dp),
                strokeColor = const(Color.White),
                hitPadding = 10.dp,
                onClick = { features ->
                    val bus = features.toBuses(positions).nearestBusTo(position)
                    if (bus != null && onBusClick != null) {
                        onBusClick(bus)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )
            SymbolLayer(
                id = "bus-lines",
                source = busSource,
                textField = feature["line"].asString(),
                textFont = const(listOf("Noto Sans Regular")),
                textSize = const(11.sp),
                textColor = feature["label"].convertToColor(),
                textHaloColor = const(Color(0x33000000)),
                textHaloWidth = const(1.dp),
            )

            val userSource = rememberGeoJsonSource(GeoJsonData.Features(FeatureCollection(userFeature)))
            CircleLayer(
                id = "user",
                source = userSource,
                radius = const(7.dp),
                color = const(Color(0xFF1A73E8)),
                strokeWidth = const(3.dp),
                strokeColor = const(Color.White),
            )
        }

    LaunchedEffect(mapState) {
        snapshotFlow { mapState.cameraPosition }.collect { rememberedCamera.position = it }
    }

    val focusKey =
        remember(stops, polylines, userLocation) {
            listOf(stops.size, polylines.size, userLocation?.latitude ?: 0.0)
        }

    var skipFirstFit by remember { mutableStateOf(restoredCamera != null) }
    LaunchedEffect(focusKey) {
        if (trackedTripId != null && followBus) return@LaunchedEffect
        if (skipFirstFit) {
            skipFirstFit = false
            return@LaunchedEffect
        }
        val points =
            polylines.flatMap { it.points }.map { GeoPoint(it.latitude, it.longitude) }.ifEmpty {
                stops
                    .take(MAX_FIT_STOPS)
                    .map { GeoPoint(it.location.latitude, it.location.longitude) }
            }
        if (points.isEmpty()) return@LaunchedEffect
        mapState.animateCameraToBounds(
            boundingBox = points.boundingBox(),
            fit = CameraFit(fitPadding = DpPadding(48.dp, 96.dp, 48.dp, 96.dp)),
            animation = CameraAnimation.Fly { duration = 1.seconds },
        )
    }

    var following by remember(trackedTripId) { mutableStateOf(trackedTripId != null && followBus) }
    var followZoomPending by remember(trackedTripId) { mutableStateOf(false) }
    LaunchedEffect(following, trackedTripId) { if (following) followZoomPending = true }

    LaunchedEffect(mapState, trackedTripId) {
        snapshotFlow { mapState.cameraMoveReason }.collect { reason ->
            if (reason is CameraMoveReason.Gesture) following = false
        }
    }

    val followPosition by rememberUpdatedState(
        ticked ?: followedBus?.let { Position(it.location.longitude, it.location.latitude) },
    )

    LaunchedEffect(following, trackedTripId) {
        if (!following) return@LaunchedEffect
        var first = true
        while (true) {
            if (!first) delay(FOLLOW_SETTLE_MILLIS.milliseconds)
            first = false
            val bus = followPosition ?: continue
            val centre = mapState.cameraPosition.center
            val target =
                followTarget(
                    bus = GeoPoint(bus.latitude, bus.longitude),
                    camera = GeoPoint(centre.latitude, centre.longitude),
                ) ?: continue
            val zoom = if (followZoomPending) FOLLOW_ZOOM else null
            followZoomPending = false
            mapState.animateCamera(
                update = CameraUpdate(center = Position(target.lon, target.lat), zoom = zoom),
                animation = CameraAnimation.Ease { duration = 1.seconds },
            )
        }
    }

    Box(modifier.fillMaxSize()) {
        MaplibreMap(
            state = mapState,
            modifier = Modifier.fillMaxSize(),
            overlay = { include(MapOverlay.AttributionOnly) },
            uiOptions = busMapUiOptions,
        )

        if (mapState.style.loadState is StyleLoadState.Pending ||
            mapState.style.loadState is StyleLoadState.Loading
        ) {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (followBus && trackedTripId != null && !following) {
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clickable { following = true },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        LogronoIcons.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.follow_bus),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
