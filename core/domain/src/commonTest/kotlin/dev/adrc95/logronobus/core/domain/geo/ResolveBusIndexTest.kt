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

package dev.adrc95.logronobus.core.domain.geo

import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ResolveBusIndexTest {
    private val stops =
        listOf(
            stop("A", 53.26, -9.10),
            stop("B", 53.26, -9.08),
            stop("C", 53.26, -9.06),
            stop("D", 53.26, -9.04),
            stop("E", 53.26, -9.02),
        )

    private fun stop(
        ref: String,
        lat: Double,
        lon: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = ref,
        shortName = ref,
        latitude = lat,
        longitude = lon,
    )

    private fun bus(
        lat: Double,
        lon: Double,
        nextStopRef: String? = null,
    ) = BusLocation(
        latitude = lat,
        longitude = lon,
        modifiedTimestamp = Instant.fromEpochMilliseconds(0),
        tripDuid = "t",
        nextStopRef = nextStopRef,
    )

    @Test
    fun nullBusIsUnknown() {
        assertEquals(-1, stops.indexOfBus(null))
    }

    @Test
    fun aBusStandingOnAStopIsMarkedAtIt() {
        assertEquals(2, stops.indexOfBus(bus(53.26, -9.06)))
    }

    @Test
    fun aBusBetweenTwoStopsIsMarkedAtTheOneAhead() {
        assertEquals(3, stops.indexOfBus(bus(53.26, -9.05)))
    }

    @Test
    fun aBusPastAStopIsMarkedAtTheNextOneEvenWhileTheFeedNamesTheLast() {
        val justLeft = bus(53.26, -9.055, nextStopRef = "C")
        assertEquals(3, stops.indexOfBus(justLeft), "past C, so the stop ahead is D")
    }

    @Test
    fun theFeedNamingAStopBehindTheBusChangesNothing() {
        assertEquals(4, stops.indexOfBus(bus(53.26, -9.02, nextStopRef = "B")))
    }

    @Test
    fun aBusOffTheRouteFallsBackToTheNearestStop() {
        assertEquals(0, stops.indexOfBus(bus(53.30, -9.30)))
    }
}
