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

package dev.adrc95.logronobus.feature.tracking.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.common.time.toClockLabel
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.TrackingView
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.mapper.toEtaDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toStopDisplayModels
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.DelayStatus
import dev.adrc95.logronobus.feature.tracking.model.NextBusDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackedDepartureDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import kotlinx.collections.immutable.toImmutableList

fun TrackingView.toDisplayModel(
    routeColors: Map<String, Color>,
    trackedShape: RouteLineDisplayModel,
    routeShapes: List<RouteLineDisplayModel>,
): TrackingDisplayModel {
    val displayStops = timelineStops.toStopDisplayModels()
    val trackedDisplay = trackedBus?.toDisplayModel(displayStops, nextStopRef)
    val departureDisplay =
        departure?.let { row ->
            TrackedDepartureDisplayModel(
                displayName = row.displayName,
                timetableId = row.timetableId,
                departTimestamp = row.departTimestamp,
                lineColor = row.timetableId?.let { code -> routeColors[code] },
            )
        }
    val mapPolyline =
        listOf(
            trackedShape.takeIf { !it.isEmpty }
                ?: routeShapes.getOrNull(directionIndex)
                ?: RouteLineDisplayModel(),
        )
    return TrackingDisplayModel(
        departure = departureDisplay,
        nextBusKnown = nextBusKnown,
        nextBus =
            nextBus?.let { row ->
                NextBusDisplayModel(
                    departTimestamp = row.departTimestamp,
                    delay = departureDelayStatus(row),
                )
            },
        timelineStops = displayStops.toImmutableList(),
        positions = listOfNotNull(trackedDisplay).toImmutableList(),
        trackedBus = trackedDisplay,
        atStopRef = atStopRef,
        busIndex = busIndex,
        nextStopRef = nextStopRef,
        stopEtas = stopEtas.toEtaDisplayModels(),
        mapEtas = listOfNotNull(nextEta).toMap().toEtaDisplayModels(),
        trackedTripId = tripId,
        trackedStopRef = riderStopRef,
        delay = departureDelayStatus(departure),
        riderStopState = riderStopState,
        completed = completed,
        mapPolyline = mapPolyline.toImmutableList(),
        riderStopName = riderStopName,
        atYourStop = atYourStop,
        passedAt = passedAt?.toClockLabel()?.takeIf { it.isNotBlank() },
    )
}

fun departureDelayStatus(departure: DepartureTime?): DelayStatus {
    val departure = departure ?: return DelayStatus.Unknown
    if (departure.predictionInaccurate) return DelayStatus.Unknown
    val status = departure.arrivalStatus?.lowercase()
    if (status == "no_report" || status == "scheduled") return DelayStatus.Unknown
    return DelayStatus.from(departure.delaySeconds)
}
