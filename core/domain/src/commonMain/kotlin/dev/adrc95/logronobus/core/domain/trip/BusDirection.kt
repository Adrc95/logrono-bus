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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.DomainConstants.MIN_MOVEMENT_METRES
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.geo.indexOfBus
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop

fun BusLocation.directionOf(
    stopRefsByDirection: List<Set<String>>,
    headsignsByDirection: List<String>,
    override: Int? = null,
): Int? {
    override?.takeIf { it in stopRefsByDirection.indices }?.let { return it }
    directionIndex?.takeIf { it in stopRefsByDirection.indices }?.let { return it }
    val ahead =
        nextStops?.map { it.stopRef }?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(nextStopRef)
    if (ahead.isNotEmpty()) {
        val overlaps = stopRefsByDirection.map { refs -> ahead.count { it in refs } }
        val best = overlaps.maxOrNull() ?: 0
        if (best > 0 && overlaps.count { it == best } == 1) return overlaps.indexOf(best)
    }
    return headsignsByDirection
        .indexOfFirst { it.isNotEmpty() && it == headsign }
        .takeIf { it >= 0 }
}

fun List<BusLocation>.forDirection(
    stopRefsByDirection: List<Set<String>>,
    headsignsByDirection: List<String>,
    direction: Int,
    overrides: Map<String, Int> = emptyMap(),
): List<BusLocation> {
    if (stopRefsByDirection.size < 2) return this
    return filter { bus ->
        val resolved =
            bus.directionOf(
                stopRefsByDirection = stopRefsByDirection,
                headsignsByDirection = headsignsByDirection,
                override = overrides[bus.tripDuid],
            )
        resolved == null || resolved == direction
    }
}

fun List<List<Stop>>.directionFromMovement(
    previous: BusLocation,
    current: BusLocation,
): Int? {
    if (size != 2) return null
    val moved =
        GeoPoint(previous.latitude, previous.longitude)
            .distanceTo(GeoPoint(current.latitude, current.longitude))
    if (moved < MIN_MOVEMENT_METRES) return null
    val seconds = (current.modifiedTimestamp - previous.modifiedTimestamp).inWholeSeconds
    if (seconds <= 0 || moved / seconds > DomainConstants.MAX_BUS_METRES_PER_SECOND) return null
    val deltas = map { stops -> stops.indexOfBus(current) - stops.indexOfBus(previous) }
    val ahead = deltas[0]
    val behind = deltas[1]
    return when {
        ahead > 0 && behind < 0 -> 0
        behind > 0 && ahead < 0 -> 1
        else -> null
    }
}
