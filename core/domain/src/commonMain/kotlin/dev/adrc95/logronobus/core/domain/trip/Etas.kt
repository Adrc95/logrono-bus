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

import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.common.time.TimeConstants
import dev.adrc95.logronobus.core.common.time.toClockLabel
import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.DomainConstants.STALE_PREDICTION_GRACE
import dev.adrc95.logronobus.core.domain.geo.indexOfBus
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopEta
import kotlinx.datetime.TimeZone
import kotlin.time.Duration
import kotlin.time.Instant

fun List<Stop>.stopEtasFor(
    trackedBus: BusLocation?,
    trackedStopRef: String? = null,
    predictions: Map<String, Instant> = emptyMap(),
    labelledAhead: Int = Int.MAX_VALUE,
    now: Instant = Instant.fromEpochMilliseconds(nowEpochMilliseconds()),
    zone: TimeZone = TimeConstants.LOCAL_ZONE,
    heading: Int? = null,
    keepLastStopHour: Boolean = false,
): Map<String, StopEta> {
    val ahead = trackedBus?.nextStops.orEmpty()
    val aheadRefs = ahead.map { it.stopRef }.toSet()
    val fromFleet =
        ahead
            .mapNotNull { prediction ->
                prediction.departureTimestamp?.let { prediction.stopRef to it }
            }.toMap()
    val predicted =
        if (predictions.isEmpty()) {
            fromFleet
        } else {
            predictions + fromFleet
        }
    val labelled =
        filter { it.stopRef in predicted }.take(labelledAhead).map { it.stopRef }.toSet() +
            setOfNotNull(trackedStopRef?.takeIf { it in predicted })
    val busHeading = heading ?: if (trackedBus == null) -1 else indexOfBus(trackedBus)
    return mapIndexedNotNull { index, stop ->
        val passed = busHeading >= 0 && index < busHeading
        val isTheLastStop = index == size - 1
        val timestamp = predicted[stop.stopRef]?.takeIf { stop.stopRef in labelled && !passed }
        val alreadyDue = timestamp?.isStaleAt(now) ?: false
        val keepHour = !alreadyDue || (keepLastStopHour && isTheLastStop)
        val label = timestamp?.takeIf { keepHour }?.toClockLabel(zone)
        when {
            label != null -> {
                stop.stopRef to StopEta(label, upcoming = !alreadyDue)
            }

            trackedBus != null && (stop.stopRef !in aheadRefs || alreadyDue || passed) -> {
                stop.stopRef to StopEta("", upcoming = false)
            }

            else -> {
                null
            }
        }
    }.toMap()
}

fun BusLocation.upcomingStops(
    stops: List<Stop>,
    now: Instant,
    limit: Int = 4,
): List<Pair<Stop, Int?>> {
    val namesByRef = stops.associateBy { it.stopRef }
    return nextStops
        .orEmpty()
        .mapNotNull { prediction ->
            val stop = namesByRef[prediction.stopRef] ?: return@mapNotNull null
            val instant = prediction.departureTimestamp
            if (instant != null && instant < now - STALE_PREDICTION_GRACE) return@mapNotNull null
            val minutes = instant?.minutesUntilFrom(now)
            stop to minutes
        }.take(limit)
}

fun List<Stop>.nextStopEta(
    etas: Map<String, StopEta>,
    busIndex: Int,
): Pair<String, StopEta>? =
    getOrNull(busIndex)?.let { stop ->
        etas[stop.stopRef]?.takeIf { it.label.isNotBlank() }?.let { stop.stopRef to it }
    }

private fun Instant.isStaleAt(now: Instant): Boolean = this < now - STALE_PREDICTION_GRACE
