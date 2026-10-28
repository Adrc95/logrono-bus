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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.trip.advanceAlongRoute
import dev.adrc95.logronobus.core.domain.trip.drawingMayMoveTo
import dev.adrc95.logronobus.core.domain.trip.extrapolationCapMs
import dev.adrc95.logronobus.core.domain.trip.metresToDraw
import dev.adrc95.logronobus.core.domain.trip.observedPace
import dev.adrc95.logronobus.core.domain.trip.routeMetresTo
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import kotlinx.coroutines.delay
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun rememberTickedBusPosition(
    trackedTripId: String?,
    followedBus: BusLocationDisplayModel?,
    followedNextStop: StopDisplayModel?,
    followedShape: List<GeoPoint>,
    followBus: Boolean,
): Position? {
    var ticked by remember { mutableStateOf<Position?>(null) }
    var lastFix by remember { mutableStateOf<Pair<GeoPoint, Long>?>(null) }
    var fixBefore by remember { mutableStateOf<Pair<GeoPoint, Long>?>(null) }

    LaunchedEffect(trackedTripId) {
        ticked = null
        lastFix = null
        fixBefore = null
    }

    LaunchedEffect(trackedTripId, followedBus?.modifiedAtMillis) {
        val bus = followedBus ?: return@LaunchedEffect
        val at = bus.modifiedAtMillis ?: return@LaunchedEffect
        if (lastFix?.second == at) return@LaunchedEffect
        fixBefore = lastFix
        lastFix = GeoPoint(bus.location.latitude, bus.location.longitude) to at
    }

    LaunchedEffect(
        trackedTripId,
        lastFix,
        fixBefore,
        followedNextStop?.stopRef,
        followedShape,
        followBus,
    ) {
        if (!followBus) {
            ticked = null
            return@LaunchedEffect
        }
        val fix = lastFix
        if (fix == null) {
            ticked = null
            return@LaunchedEffect
        }
        val next = followedNextStop
        val target =
            next?.let { GeoPoint(it.location.latitude, it.location.longitude) }
                ?: followedShape.lastOrNull()
        if (target == null || followedShape.size < 2) {
            ticked = null
            return@LaunchedEffect
        }
        val fromMetres = routeMetresTo(followedShape, fix.first)
        val remaining = routeMetresTo(followedShape, target) - fromMetres
        if (remaining <= 0.0) {
            ticked = null
            return@LaunchedEffect
        }

        val gapMs = fixBefore?.let { fix.second - it.second } ?: 0L
        val pace =
            observedPace(
                metresAlongRoute =
                    fixBefore
                        ?.let {
                            routeMetresTo(followedShape, fix.first) -
                                routeMetresTo(
                                    followedShape,
                                    it.first,
                                )
                        }
                        ?: 0.0,
                seconds = gapMs / 1000.0,
            )
        val cap = extrapolationCapMs(gapMs)

        while (true) {
            val metres =
                metresToDraw(
                    pace,
                    (nowEpochMilliseconds() - fix.second).coerceAtMost(cap),
                    remaining,
                )
            val candidate = advanceAlongRoute(followedShape, fix.first, target, metres / remaining)
            val drawnBefore =
                ticked?.let { routeMetresTo(followedShape, GeoPoint(it.latitude, it.longitude)) }
            if (drawingMayMoveTo(routeMetresTo(followedShape, candidate), drawnBefore)) {
                ticked = Position(candidate.lon, candidate.lat)
            }
            delay(1_000.milliseconds)
        }
    }
    return ticked
}
