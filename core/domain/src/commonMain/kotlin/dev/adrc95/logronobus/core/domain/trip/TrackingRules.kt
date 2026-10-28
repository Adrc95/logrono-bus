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
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.geo.indexOfBus
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop

fun nearestLineBus(
    positions: List<BusLocation>,
    stop: Stop?,
    lineCode: String?,
    direction: Int,
    stopsByDirection: List<List<Stop>>,
    headsignsByDirection: List<String>,
): BusLocation? {
    stop ?: return null
    val stopRefsByDirection =
        stopsByDirection.map { dir -> dir.mapTo(mutableSetOf()) { it.stopRef } }
    val candidates =
        positions
            .filter { lineCode == null || it.timetableId == lineCode }
            .filter { it.latitude != 0.0 || it.longitude != 0.0 }
    val sameDirection =
        candidates.filter { bus ->
            val resolved = bus.directionOf(stopRefsByDirection, headsignsByDirection)
            resolved == null || resolved == direction
        }
    val directionStops = stopsByDirection.getOrNull(direction).orEmpty()
    val stillAhead = sameDirection.filter { it.isStillAheadOf(directionStops, stop) }
    return (stillAhead.ifEmpty { sameDirection.ifEmpty { candidates } })
        .minByOrNull {
            GeoPoint(it.latitude, it.longitude).distanceTo(
                GeoPoint(stop.latitude, stop.longitude),
            )
        }
}

fun BusLocation.isStillAheadOf(
    directionStops: List<Stop>,
    stop: Stop?,
    heading: Int? = null,
): Boolean {
    stop ?: return true
    val waitingAt = directionStops.indexOfFirst { it.stopRef == stop.stopRef }
    if (waitingAt < 0) return true
    val feedHeading = heading ?: -1
    val geometricHeading = directionStops.indexOfBus(this)
    val awayFromStop =
        GeoPoint(latitude, longitude)
            .distanceTo(GeoPoint(stop.latitude, stop.longitude)) > DomainConstants.AT_STOP_METRES
    val passed =
        feedHeading > waitingAt || (geometricHeading > waitingAt && awayFromStop)
    return !passed
}

fun settleBusIndex(
    rawIndex: Int,
    previousIndex: Int,
    metresPastStop: Double?,
): Int {
    val past = metresPastStop ?: Double.MAX_VALUE
    return when {
        previousIndex < 0 || rawIndex < 0 || rawIndex == previousIndex -> {
            rawIndex
        }

        rawIndex > previousIndex -> {
            if (rawIndex - previousIndex == 1 && past <= DomainConstants.BUS_INDEX_SETTLE_METRES) {
                previousIndex
            } else {
                rawIndex
            }
        }

        past < DomainConstants.BUS_INDEX_BACKTRACK_METRES -> {
            previousIndex
        }

        else -> {
            rawIndex
        }
    }
}

fun resolveDirectionIndex(
    routeStops: List<List<Stop>>,
    directionIndex: Int?,
    stopRef: String?,
): Int =
    directionIndex?.takeIf { it in routeStops.indices }
        ?: routeStops.indexOfFirst { dir -> dir.any { it.stopRef == stopRef } }.takeIf { it >= 0 }
        ?: 0

fun nextStopRefOf(
    bus: BusLocation?,
    stops: List<Stop>,
): String? =
    bus?.let { tracked ->
        tracked.nextStopRef?.takeIf { ref -> stops.any { it.stopRef == ref } }
            ?: stops.getOrNull(stops.indexOfBus(tracked))?.stopRef
    }

fun arrivingStopName(
    bus: BusLocation?,
    targetStopRef: String?,
    stops: List<Stop>,
): String? {
    bus ?: return null
    val target =
        targetStopRef?.let { ref -> stops.firstOrNull { it.stopRef == ref } } ?: return null
    val heading = stops.indexOfBus(bus)
    if (heading < 0 || stops[heading].stopRef != target.stopRef) return null
    return target.longName
}
