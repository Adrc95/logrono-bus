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

package dev.adrc95.logronobus

import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.domain.trip.directionOf
import dev.adrc95.logronobus.core.domain.trip.hasTripCompleted
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackingViewUseCase
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.TrackingViewInput
import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.feature.tracking.mapper.toDisplayModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class LiveTripCompletionProbeIntegrationTest {
    @BeforeTest
    fun require_live_api() {
        org.junit.Assume.assumeTrue(System.getenv("LIVE_API") == "1")
    }

    @Test
    fun `given a bus at the end of its direction when the trip is read then it is finished and not in the opposite one`() =
        runTest {
            val repository = TestBus()
            val detail = GetRouteDetailUseCase(repository.catalogue)
            val details = mutableMapOf<String, dev.adrc95.logronobus.core.model.RouteDetail>()
            var verified = 0
            var closestSoFar = Double.MAX_VALUE
            var closestNow: Double
            var closestLabel = ""
            for (sample in 1..12) {
                if (sample > 1) delay(20_000.milliseconds)
                val buses = repository.vehicles.getBusPositions(forceRefresh = sample > 1).all
                closestNow = Double.MAX_VALUE
                closestLabel = ""
                println("live: sample $sample, ${buses.size} buses")
                for (bus in buses) {
                    val code = bus.timetableId ?: continue
                    val route = details.getOrPut(code) { detail(code) }
                    if (route.stops.size < 2) continue
                    val refsByDirection =
                        route.stops.map { stops -> stops.mapTo(mutableSetOf()) { it.stopRef } }
                    val direction = bus.directionOf(refsByDirection, route.headsigns) ?: continue
                    val stops: List<Stop> = route.stops.getOrNull(direction) ?: continue
                    if (stops.size < 2) continue
                    val terminus = stops.last()
                    val away =
                        GeoPoint(bus.latitude, bus.longitude)
                            .distanceTo(GeoPoint(terminus.latitude, terminus.longitude))
                    closestSoFar = minOf(closestSoFar, away)
                    if (away < closestNow) {
                        closestNow = away
                        closestLabel = "line $code bus ${bus.vehicleId} dir $direction"
                    }
                    if (!stops.hasTripCompleted(bus)) continue
                    val otherStops = route.stops.getOrNull(1 - direction).orEmpty()
                    val departure =
                        DepartureTime(
                            displayName = terminus.longName,
                            timetableId = code,
                            delaySeconds = 0,
                            tripId = bus.tripDuid,
                            vehicleId = bus.vehicleId,
                            directionIndex = direction,
                        )
                    val display =
                        TrackingViewInput(
                            routeStops = route.stops,
                            directionHeadsigns = route.headsigns,
                            departure = departure,
                            tripId = bus.tripDuid,
                            stopRef = terminus.stopRef,
                            positions = listOf(bus),
                        ).display()
                    println(
                        "live: line $code bus ${bus.vehicleId} at ${"%.0f".format(away)} m of " +
                            "${terminus.longName} (dir $direction) -> completed=${display.completed}",
                    )
                    assertTrue(
                        display.completed,
                        "line $code bus ${bus.vehicleId} is at the end of its direction, the screen must mark it",
                    )
                    assertTrue(
                        otherStops.isEmpty() || !otherStops.hasTripCompleted(bus),
                        "the same vehicle turning around is not a finished trip of the other direction",
                    )
                    verified++
                }
                println(
                    "live: closest to a terminus now: $closestLabel at ${
                        "%.0f".format(
                            closestNow,
                        )
                    } m",
                )
                if (verified > 0) break
            }
            if (verified == 0) {
                println(
                    "live: no bus reached a terminus during the window; closest one was " +
                        "${"%.0f".format(closestSoFar)} m away",
                )
            } else {
                println("live: $verified finished bus(es) verified against the feed")
            }
        }
}

private fun TrackingViewInput.display() =
    GetTrackingViewUseCase()(this).toDisplayModel(
        routeColors = emptyMap(),
        trackedShape = RouteLineDisplayModel(),
        routeShapes = emptyList(),
    )
