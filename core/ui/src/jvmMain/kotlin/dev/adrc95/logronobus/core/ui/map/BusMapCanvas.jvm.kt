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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.theme.isDarkTheme
import dev.adrc95.logronobus.core.domain.geo.bearingTo
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.math.sign

@Composable
fun BusMapCanvasView(
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
    fun stopWorthShowingAtAnyZoom(stopRef: String) =
        stopRef == trackedStopRef || stopEtas[stopRef]?.label?.isNotBlank() == true

    val tileImages = remember { mutableStateMapOf<TileId, ImageBitmap>() }
    val tileOrder = remember { ArrayDeque<TileId>() }
    val scope = rememberCoroutineScope()
    val fetching = remember { mutableSetOf<TileId>() }
    val visibleTiles = remember { mutableSetOf<TileId>() }
    val tileGate = remember { Semaphore(TILE_FETCH_CONCURRENCY) }

    var zoom by remember { mutableStateOf(DEFAULT_ZOOM) }
    var centerLat by remember { mutableStateOf(LOGRONO_LAT) }
    var centerLon by remember { mutableStateOf(LOGRONO_LON) }

    var hasCentered by remember { mutableStateOf(false) }
    var hoveredBus by remember { mutableStateOf<BusLocationDisplayModel?>(null) }
    var hoveredStop by remember { mutableStateOf<StopDisplayModel?>(null) }
    var hoverPos by remember { mutableStateOf(Offset.Zero) }

    val animatedPositions = remember { mutableStateMapOf<String, LatLon>() }

    val headings = remember { mutableStateMapOf<String, Float>() }

    val drawnFixAt = remember { mutableMapOf<String, Long>() }
    LaunchedEffect(positions) {
        val targets = positions.associateBy { it.markerKey }
        animatedPositions.keys.retainAll(targets.keys)
        headings.keys.retainAll(targets.keys)
        drawnFixAt.keys.retainAll(targets.keys)

        val starts = HashMap<String, LatLon>(targets.size)
        val ends = HashMap<String, LatLon>(targets.size)
        targets.forEach { (id, bus) ->
            val to = LatLon(bus.location.latitude, bus.location.longitude)
            val fixAt = bus.modifiedAtMillis
            val drawnAt = drawnFixAt[id]
            val from = animatedPositions[id]

            if (fixAt != null && drawnAt != null && fixAt < drawnAt) return@forEach

            if (from != null && GeoPoint(from.lat, from.lon).distanceTo(
                    GeoPoint(
                        to.lat,
                        to.lon,
                    ),
                ) < JITTER_DEADBAND_METRES
            ) {
                drawnFixAt[id] = maxOf(drawnAt ?: Long.MIN_VALUE, fixAt ?: Long.MIN_VALUE)
                return@forEach
            }
            if (from != null && GeoPoint(from.lat, from.lon).distanceTo(
                    GeoPoint(
                        to.lat,
                        to.lon,
                    ),
                ) >= MIN_MOVE_METRES
            ) {
                GeoPoint(from.lat, from.lon)
                    .bearingTo(GeoPoint(to.lat, to.lon))
                    ?.let { headings[id] = it }
            }
            if (fixAt != null) drawnFixAt[id] = fixAt
            starts[id] = from ?: to
            ends[id] = to
        }
        if (ends.isEmpty()) return@LaunchedEffect

        val begin = withFrameNanos { it }
        while (true) {
            val frame = withFrameNanos { it }
            val progress =
                ((frame - begin) / 1_000_000.0 / GLIDE_MILLIS).coerceIn(0.0, 1.0)

            val t = 1.0 - (1.0 - progress) * (1.0 - progress)
            ends.forEach { (id, end) ->
                val start = starts[id] ?: end
                animatedPositions[id] =
                    LatLon(
                        lat = start.lat + (end.lat - start.lat) * t,
                        lon = start.lon + (end.lon - start.lon) * t,
                    )
            }
            if (progress >= 1.0) break
        }
    }

    val trackedPulse = rememberInfiniteTransition(label = "tracked-bus")
    val trackedHaloAlpha by trackedPulse.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.42f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "tracked-bus-halo-alpha",
    )
    val trackedHaloRadius by trackedPulse.animateFloat(
        initialValue = 26f,
        targetValue = 40f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "tracked-bus-halo-radius",
    )
    var following by remember(trackedTripId) { mutableStateOf(trackedTripId != null && followBus) }
    var followZoomPending by remember(trackedTripId) { mutableStateOf(trackedTripId != null && followBus) }
    val positionsNow by rememberUpdatedState(positions)
    LaunchedEffect(trackedTripId, following) {
        if (!following) return@LaunchedEffect
        while (true) {
            val bus = trackedTripId?.let { id -> positionsNow.find { it.tripDuid == id } }
            val drawn =
                bus?.let { animatedPositions[it.markerKey] ?: LatLon(it.location.latitude, it.location.longitude) }
            val target =
                followTarget(
                    bus = drawn?.let { GeoPoint(it.lat, it.lon) },
                    camera = GeoPoint(centerLat, centerLon),
                )
            if (target != null) {
                if (followZoomPending) {
                    zoom = MAX_ZOOM - 1
                    followZoomPending = false
                }
                val fromLat = centerLat
                val fromLon = centerLon
                animate(0f, 1f, animationSpec = tween(900)) { progress, _ ->
                    centerLat = fromLat + (target.lat - fromLat) * progress
                    centerLon = fromLon + (target.lon - fromLon) * progress
                }
            }
            delay(FOLLOW_SETTLE_MILLIS)
        }
    }

    LaunchedEffect(positions, trackedTripId, trackedStopRef, userLocation) {
        if (trackedTripId != null && followBus) return@LaunchedEffect

        if (userLocation != null && !hasCentered) {
            centerLat = userLocation.latitude
            centerLon = userLocation.longitude
            zoom = STOP_MARKER_MIN_ZOOM + 2
            hasCentered = true
        } else if (trackedStopRef != null && !hasCentered) {
            stops.find { it.stopRef == trackedStopRef }?.let { stop ->
                centerLat = stop.location.latitude
                centerLon = stop.location.longitude
                zoom = MAX_ZOOM - 1
                hasCentered = true
            }
        } else if (positions.isNotEmpty() && !hasCentered) {
            centerLat = positions.map { it.location.latitude }.average()
            centerLon = positions.map { it.location.longitude }.average()
            hasCentered = true
        }
    }

    val busColors =
        remember(positions) {
            positions
                .groupBy { it.timetableId ?: "" }
                .mapValues { (route, buses) ->
                    buses.firstNotNullOfOrNull { it.lineColor } ?: busColor(route)
                }
        }

    val routeHeadsigns =
        remember(positions) {
            positions
                .groupBy { it.timetableId ?: "" }
                .mapValues { (_, buses) -> buses.mapNotNull { it.headsign }.distinct().sorted() }
        }
    val busIcon = remember { busGlyph() }
    val busPainter = rememberVectorPainter(busIcon)
    val darkTheme = isDarkTheme()
    val darkNow by rememberUpdatedState(darkTheme)
    LaunchedEffect(darkTheme) {
        tileImages.clear()
        tileOrder.clear()
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle =
        with(LocalDensity.current) {
            TextStyle(fontSize = 15f.toSp(), fontWeight = FontWeight.Bold)
        }
    val routeLabelLayouts =
        remember(positions, textMeasurer, labelStyle) {
            positions
                .mapNotNull { it.timetableId }
                .filter { it.isNotEmpty() }
                .distinct()
                .associateWith { route -> textMeasurer.measure(AnnotatedString(route), labelStyle) }
        }
    val etaStyle =
        with(LocalDensity.current) {
            TextStyle(fontSize = 12f.toSp(), fontWeight = FontWeight.Bold)
        }
    val etaLayouts =
        remember(stopEtas, textMeasurer, etaStyle) {
            stopEtas
                .filterValues { it.label.isNotBlank() }
                .mapValues { (_, eta) -> textMeasurer.measure(AnnotatedString(eta.label), etaStyle) }
        }

    Box(modifier.clipToBounds()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }

            var lastTrackedTripId by remember { mutableStateOf(trackedTripId) }
            LaunchedEffect(trackedTripId, stops, widthPx, heightPx) {
                val justStoppedTracking = lastTrackedTripId != null && trackedTripId == null
                lastTrackedTripId = trackedTripId
                if (justStoppedTracking && stops.isNotEmpty() && widthPx > 0f && heightPx > 0f) {
                    val lats = stops.map { it.location.latitude }
                    val lons = stops.map { it.location.longitude }
                    val minLat = lats.min()
                    val maxLat = lats.max()
                    val minLon = lons.min()
                    val maxLon = lons.max()
                    centerLat = (minLat + maxLat) / 2.0
                    centerLon = (minLon + maxLon) / 2.0

                    var fitZoom = MIN_ZOOM
                    for (z in MAX_ZOOM downTo MIN_ZOOM) {
                        val spanXf = lonToTileXf(maxLon, z) - lonToTileXf(minLon, z)
                        val spanYf = latToTileYf(minLat, z) - latToTileYf(maxLat, z)
                        if (spanXf * TILE_PX <= widthPx * 0.8 && spanYf * TILE_PX <= heightPx * 0.8) {
                            fitZoom = z
                            break
                        }
                    }
                    zoom = fitZoom
                }
            }

            val zoomAt: DoubleInputAction<Offset, Int> = { pivot, requestedZoom ->
                val newZoom = requestedZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
                if (newZoom != zoom) {
                    val pivotXf =
                        lonToTileXf(
                            centerLon,
                            zoom,
                        ) + (pivot.x - widthPx / 2.0) / TILE_PX
                    val pivotYf =
                        latToTileYf(
                            centerLat,
                            zoom,
                        ) + (pivot.y - heightPx / 2.0) / TILE_PX
                    val pivotLon = tileXfToLon(pivotXf, zoom)
                    val pivotLat = tileYfToLat(pivotYf, zoom)
                    val newCenterXf =
                        lonToTileXf(
                            pivotLon,
                            newZoom,
                        ) - (pivot.x - widthPx / 2.0) / TILE_PX
                    val newCenterYf =
                        latToTileYf(
                            pivotLat,
                            newZoom,
                        ) - (pivot.y - heightPx / 2.0) / TILE_PX
                    centerLon = tileXfToLon(newCenterXf, newZoom)
                    centerLat = tileYfToLat(newCenterYf, newZoom)
                    zoom = newZoom
                }
            }

            val centerTileXf = lonToTileXf(centerLon, zoom)
            val centerTileYf = latToTileYf(centerLat, zoom)
            val originTileXf = centerTileXf - widthPx / 2.0 / TILE_PX
            val originTileYf = centerTileYf - heightPx / 2.0 / TILE_PX

            val x0 = originTileXf.toInt()
            val y0 = originTileYf.toInt()
            val x1 = (originTileXf + widthPx / TILE_PX).toInt() + 1
            val y1 = (originTileYf + heightPx / TILE_PX).toInt() + 1

            LaunchedEffect(x0, y0, x1, y1, zoom, darkTheme) {
                visibleTiles.clear()
                for (tx in x0..x1) {
                    for (ty in y0..y1) {
                        val id = TileId(zoom, tx, ty)
                        if (id.inWorld()) visibleTiles.add(id)
                    }
                }

                val wanted =
                    buildList {
                        addAll(visibleTiles)
                        for (ring in 1..TILE_PREFETCH_RING) {
                            for (tx in x0 - ring..x1 + ring) {
                                for (ty in y0 - ring..y1 + ring) {
                                    val id = TileId(zoom, tx, ty)
                                    if (id.inWorld() && id !in visibleTiles) add(id)
                                }
                            }
                        }
                    }

                for (id in wanted) {
                    if (id in tileImages || !fetching.add(id)) continue
                    scope.launch {
                        try {
                            tileGate.withPermit {
                                val bytes =
                                    withContext(Dispatchers.IO) {
                                        fetchTile(tileUrl(id))
                                    } ?: return@withPermit
                                val decoded =
                                    withContext(Dispatchers.Default) {
                                        val raw = bytes.toImageBitmap()
                                        if (darkTheme) raw.withFilter(DarkTileFilter) else raw
                                    }
                                if (darkTheme != darkNow) return@withPermit
                                tileImages[id] = decoded
                                tileOrder.addLast(id)
                                while (tileOrder.size > TILE_CACHE_MAX) {
                                    val oldest = tileOrder.removeFirstOrNull() ?: break
                                    if (oldest in visibleTiles) {
                                        tileOrder.addLast(oldest)
                                        break
                                    }
                                    tileImages.remove(oldest)
                                }
                            }
                        } catch (_: Exception) {
                        } finally {
                            fetching.remove(id)
                        }
                    }
                }
            }

            Canvas(
                Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        var pinchAccum = 1f
                        detectTransformGestures { centroid, pan, gestureZoom, _ ->
                            if (pan != Offset.Zero) {
                                if (gestureZoom == 1f) following = false
                                val newXf =
                                    lonToTileXf(centerLon, zoom) - pan.x / TILE_PX
                                val newYf =
                                    latToTileYf(centerLat, zoom) - pan.y / TILE_PX
                                centerLon = tileXfToLon(newXf, zoom)
                                centerLat = tileYfToLat(newYf, zoom)
                            }
                            if (gestureZoom != 1f) {
                                pinchAccum *= gestureZoom
                                if (pinchAccum > 1.4f) {
                                    zoomAt(centroid, zoom + 1)
                                    pinchAccum = 1f
                                } else if (pinchAccum < 0.71f) {
                                    zoomAt(centroid, zoom - 1)
                                    pinchAccum = 1f
                                }
                            }
                        }
                    }.pointerInput(
                        positions,
                        stops,
                        trackedStopRef,
                        onStopClick,
                        onBusClick,
                        widthPx,
                        heightPx,
                    ) {
                        detectTapGestures(
                            onDoubleTap = { pos -> zoomAt(pos, zoom + 1) },
                            onTap = tap@{ pos ->
                                if (onStopClick == null && onBusClick == null) return@tap
                                val originXf = lonToTileXf(centerLon, zoom) - (widthPx / 2.0 / TILE_PX)
                                val originYf = latToTileYf(centerLat, zoom) - (heightPx / 2.0 / TILE_PX)

                                fun screen(
                                    lat: Double,
                                    lon: Double,
                                ) = projectToScreen(lat, lon, zoom, originXf, originYf)
                                if (onBusClick != null) {
                                    val bus =
                                        busAt(
                                            position = pos,
                                            buses = positions,
                                            projected = { bus ->
                                                val drawn =
                                                    animatedPositions[bus.markerKey]
                                                        ?: LatLon(bus.location.latitude, bus.location.longitude)
                                                screen(drawn.lat, drawn.lon)
                                            },
                                            radiusPx = TAP_BUS_RADIUS,
                                        )
                                    if (bus != null) {
                                        onBusClick(bus)
                                        return@tap
                                    }
                                }
                                if (onStopClick == null) return@tap
                                val stop =
                                    stopAt(
                                        position = pos,
                                        stops = stops,
                                        projected = { stop ->
                                            screen(stop.location.latitude, stop.location.longitude)
                                        },
                                        radiusPx = { TAP_STOP_RADIUS },
                                        isVisible = { stop ->
                                            zoom >= STOP_MARKER_MIN_ZOOM ||
                                                stopWorthShowingAtAnyZoom(stop.stopRef)
                                        },
                                    )
                                stop?.let(onStopClick)
                            },
                        )
                    }.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    val change = event.changes.firstOrNull()
                                    val dy = change?.scrollDelta?.y ?: 0f
                                    if (change != null && dy != 0f) {
                                        zoomAt(change.position, zoom - dy.sign.toInt())
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }.pointerInput(positions, stops, zoom, centerLat, centerLon, widthPx, heightPx) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Move, PointerEventType.Enter -> {
                                        val position = event.changes.first().position
                                        hoverPos = position
                                        val originXf =
                                            lonToTileXf(centerLon, zoom) - (widthPx / 2.0 / TILE_PX)
                                        val originYf =
                                            latToTileYf(centerLat, zoom) - (heightPx / 2.0 / TILE_PX)

                                        fun screen(
                                            lat: Double,
                                            lon: Double,
                                        ) = projectToScreen(lat, lon, zoom, originXf, originYf)
                                        val bus =
                                            busAt(
                                                position = position,
                                                buses = positions,
                                                projected = { bus ->
                                                    val drawn =
                                                        animatedPositions[bus.markerKey]
                                                            ?: LatLon(
                                                                bus.location.latitude,
                                                                bus.location.longitude,
                                                            )
                                                    screen(drawn.lat, drawn.lon)
                                                },
                                                radiusPx = TAP_BUS_RADIUS,
                                            )
                                        hoveredBus = bus
                                        hoveredStop =
                                            if (bus != null) {
                                                null
                                            } else {
                                                stopAt(
                                                    position = position,
                                                    stops = stops,
                                                    projected = { stop ->
                                                        screen(
                                                            stop.location.latitude,
                                                            stop.location.longitude,
                                                        )
                                                    },
                                                    radiusPx = { stop ->
                                                        val radius =
                                                            if (stop.stopRef == trackedStopRef) {
                                                                TRACKED_STOP_RADIUS
                                                            } else {
                                                                STOP_MARKER_RADIUS
                                                            }
                                                        radius + HOVER_STOP_SLACK
                                                    },
                                                    isVisible = { stop ->
                                                        zoom >= STOP_MARKER_MIN_ZOOM ||
                                                            stopWorthShowingAtAnyZoom(stop.stopRef)
                                                    },
                                                )
                                            }
                                    }

                                    PointerEventType.Exit -> {
                                        hoveredBus = null
                                        hoveredStop = null
                                    }
                                }
                            }
                        }
                    },
            ) {
                for (tx in x0..x1) {
                    for (ty in y0..y1) {
                        val px = ((tx - originTileXf) * TILE_PX).toFloat()
                        val py = ((ty - originTileYf) * TILE_PX).toFloat()
                        drawTileOrAncestor(TileId(zoom, tx, ty), Offset(px, py), tileImages)
                    }
                }

                polylines.forEach { line ->
                    if (line.points.size < 2) return@forEach
                    drawRouteLine(
                        points =
                            line.points.map {
                                projectToScreen(
                                    it.latitude,
                                    it.longitude,
                                    zoom,
                                    originTileXf,
                                    originTileYf,
                                )
                            },
                        color = routeColor ?: ROUTE_LINE_COLOR,
                    )
                }
                stops.forEach { stop ->
                    if (zoom < STOP_MARKER_MIN_ZOOM && !stopWorthShowingAtAnyZoom(stop.stopRef)) {
                        return@forEach
                    }
                    val at =
                        projectToScreen(
                            stop.location.latitude,
                            stop.location.longitude,
                            zoom,
                            originTileXf,
                            originTileYf,
                        )
                    val onScreen =
                        at.x in -STOP_MARKER_MARGIN..size.width + STOP_MARKER_MARGIN &&
                            at.y in -STOP_MARKER_MARGIN..size.height + STOP_MARKER_MARGIN
                    if (!onScreen) return@forEach
                    val eta = stopEtas[stop.stopRef]
                    drawStopMarker(
                        center = at,
                        isTracked = stop.stopRef == trackedStopRef,
                        isPastEta = eta != null && !eta.upcoming,
                        label = etaLayouts[stop.stopRef],
                    )
                }
                positions.forEach { bus ->
                    val drawn =
                        animatedPositions[bus.markerKey]
                            ?: LatLon(bus.location.latitude, bus.location.longitude)
                    drawBusMarker(
                        center =
                            projectToScreen(
                                drawn.lat,
                                drawn.lon,
                                zoom,
                                originTileXf,
                                originTileYf,
                            ),
                        color = busColors[bus.timetableId ?: ""] ?: BUS_PALETTE.first(),
                        headingDegrees = bus.headingDegrees ?: headings[bus.markerKey],
                        isTracked = bus.tripDuid == trackedTripId,
                        haloAlpha = trackedHaloAlpha,
                        haloRadius = trackedHaloRadius,
                        isReturnDirection =
                            routeHeadsigns[bus.timetableId ?: ""]
                                ?.indexOf(bus.headsign) == 1,
                        label = routeLabelLayouts[bus.timetableId],
                        glyph = busPainter,
                    )
                }

                userLocation?.let { loc ->
                    val at =
                        projectToScreen(
                            loc.latitude,
                            loc.longitude,
                            zoom,
                            originTileXf,
                            originTileYf,
                        )
                    val onScreen =
                        at.x in -USER_LOCATION_MARGIN..size.width + USER_LOCATION_MARGIN &&
                            at.y in -USER_LOCATION_MARGIN..size.height + USER_LOCATION_MARGIN
                    if (onScreen) drawUserLocation(at)
                }
            }
            MapHoverCard(
                modifier =
                    Modifier.offset {
                        IntOffset(
                            (hoverPos.x + 16).toInt().coerceAtMost((widthPx - 160).toInt()),
                            (hoverPos.y + 16).toInt().coerceAtMost((heightPx - 80).toInt()),
                        )
                    },
                bus = hoveredBus,
                stop = hoveredStop,
            )
        }
        if (followBus && trackedTripId != null && !following) {
            FollowBusChip(
                modifier = Modifier.align(Alignment.TopStart),
                onFollow = { following = true },
            )
        }
        val legendEntries = busColors.filterKeys { it.isNotEmpty() }
        if (legendEntries.size >= 2) {
            MapLegend(
                modifier = Modifier.align(Alignment.TopEnd),
                entries = legendEntries,
            )
        }
    }
}
