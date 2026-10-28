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

package dev.adrc95.logronobus.core.domain.usecase

import dev.adrc95.logronobus.core.common.time.parseInstant
import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.geo.indexOfBus
import dev.adrc95.logronobus.core.domain.geo.isStandingStill
import dev.adrc95.logronobus.core.domain.geo.stopAt
import dev.adrc95.logronobus.core.domain.trip.hasTripCompleted
import dev.adrc95.logronobus.core.domain.trip.isStillAheadOf
import dev.adrc95.logronobus.core.domain.trip.nearestLineBus
import dev.adrc95.logronobus.core.domain.trip.nextStopEta
import dev.adrc95.logronobus.core.domain.trip.nextStopRefOf
import dev.adrc95.logronobus.core.domain.trip.resolveDirectionIndex
import dev.adrc95.logronobus.core.domain.trip.settleBusIndex
import dev.adrc95.logronobus.core.domain.trip.stopEtasFor
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.RiderStopState
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.TrackingView
import dev.adrc95.logronobus.core.model.TrackingViewInput
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class GetTrackingViewUseCase {
    operator fun invoke(input: TrackingViewInput): TrackingView {
        val positions = input.positions.distinctBy { it.tripDuid }
        val directionIndex =
            resolveDirectionIndex(
                routeStops = input.routeStops,
                directionIndex = input.departure?.directionIndex,
                stopRef = input.stopRef,
            )
        val timelineStops = input.routeStops.getOrNull(directionIndex).orEmpty()
        val vehicle = input.departure?.vehicleId?.takeIf { it.isNotBlank() }
        val trip = input.tripId?.takeIf { it.isNotBlank() }
        val confirmed =
            positions.filter { bus ->
                (vehicle != null && bus.vehicleId == vehicle) || (trip != null && bus.tripDuid == trip)
            }
        val stopPoint = input.routeStops.flatten().firstOrNull { it.stopRef == input.stopRef }
        val nearest =
            nearestLineBus(
                positions = positions,
                stop = stopPoint,
                lineCode = input.departure?.timetableId,
                direction = directionIndex,
                stopsByDirection = input.routeStops,
                headsignsByDirection = input.directionHeadsigns,
            )
        val preferred = input.preferredVehicleId?.takeIf { it.isNotBlank() }
        val remembered = preferred?.let { id -> positions.firstOrNull { it.vehicleId == id } }
        val followedBus = confirmed.firstOrNull() ?: remembered
        val trackedBus =
            when {
                followedBus != null -> followedBus
                preferred != null -> null
                else -> nearest
            }
        val nearestStopRef = timelineStops.stopAt(trackedBus)
        val atStopRef =
            when {
                trackedBus == null -> null
                nearestStopRef == null -> null
                trackedBus.isStandingStill(input.previousPositions) -> nearestStopRef
                trackedBus.nextStopRef == nearestStopRef -> nearestStopRef
                else -> null
            }
        val feedNextIndex =
            trackedBus?.nextStopRef?.let { ref ->
                timelineStops.indexOfFirst { it.stopRef == ref }
            } ?: -1
        val geometricIndex = timelineStops.indexOfBus(trackedBus)
        val metresFromFeedStop =
            trackedBus?.let { bus ->
                timelineStops.getOrNull(feedNextIndex)?.let { stop ->
                    GeoPoint(bus.latitude, bus.longitude)
                        .distanceTo(GeoPoint(stop.latitude, stop.longitude))
                }
            }
        val feedLagging =
            feedNextIndex >= 0 &&
                geometricIndex > feedNextIndex &&
                (metresFromFeedStop ?: 0.0) > DomainConstants.AT_STOP_METRES
        val previousPosition =
            trackedBus?.let { bus ->
                input.previousPositions.firstOrNull { it.tripDuid == bus.tripDuid }
            }
        val previousIndex =
            previousPosition?.let { before -> timelineStops.indexOfBus(before) } ?: -1
        val metresFromPreviousStop =
            trackedBus?.let { bus ->
                timelineStops.getOrNull(previousIndex)?.let { stop ->
                    GeoPoint(bus.latitude, bus.longitude)
                        .distanceTo(GeoPoint(stop.latitude, stop.longitude))
                }
            }
        val busIndex =
            when {
                feedLagging -> {
                    geometricIndex
                }

                feedNextIndex >= 0 -> {
                    feedNextIndex
                }

                else -> {
                    settleBusIndex(
                        rawIndex = geometricIndex,
                        previousIndex = previousIndex,
                        metresPastStop = metresFromPreviousStop,
                    )
                }
            }
        val nextStopRef = nextStopRefOf(followedBus, timelineStops)
        val riderStopState =
            when {
                trackedBus == null || stopPoint == null -> RiderStopState.Unknown
                atStopRef != null && atStopRef == stopPoint.stopRef -> RiderStopState.Approaching
                trackedBus.isStillAheadOf(timelineStops, stopPoint, busIndex) -> RiderStopState.Approaching
                followedBus != null -> RiderStopState.Passed
                else -> RiderStopState.Unknown
            }
        val now = Instant.fromEpochMilliseconds(input.nowMs)
        val stopEtas =
            timelineStops.stopEtasFor(
                trackedBus = trackedBus,
                trackedStopRef = input.stopRef,
                predictions = input.predictions,
                now = now,
                heading = busIndex,
                keepLastStopHour = input.tripCompleted,
            )
        val resolvedDeparture =
            input.departure?.let { row ->
                row.copy(
                    departTimestamp =
                        input.stopRef?.let { ref -> input.predictions[ref] }
                            ?: row.departTimestamp,
                )
            }
        return TrackingView(
            directionIndex = directionIndex,
            followedBus = followedBus,
            trackedBus = trackedBus,
            busIndex = busIndex,
            atStopRef = atStopRef,
            nextStopRef = nextStopRef,
            stopEtas = stopEtas,
            nextEta = timelineStops.nextStopEta(stopEtas, busIndex),
            riderStopState = riderStopState,
            passedAt =
                input.departure
                    ?.departTimestamp
                    ?.takeIf { stamp -> stamp <= now },
            completed = input.tripCompleted || timelineStops.hasTripCompleted(followedBus),
            tripId = trackedBus?.tripDuid ?: trip,
            timelineStops = timelineStops,
            riderStopRef = input.stopRef,
            riderStopName =
                input.stopRef?.let { ref ->
                    timelineStops.firstOrNull { it.stopRef == ref }?.longName
                },
            departure = resolvedDeparture,
            nextBus = input.nextBus,
            nextBusKnown = input.nextBusKnown,
            atYourStop = atStopRef != null && atStopRef == input.stopRef,
        )
    }
}
