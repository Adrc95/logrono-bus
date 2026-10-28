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

import dev.adrc95.logronobus.core.domain.DomainConstants.TRIP_MATCH_TOLERANCE_SECONDS
import dev.adrc95.logronobus.core.model.Arrival
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TripPinningTest {
    private val riderStop = "22"
    private val dueAtStop = Instant.parse("2026-10-19T17:11:00+02:00")

    private fun row(
        vehicle: String?,
        stop: String?,
        expected: Instant?,
    ) = Arrival(
        lineRef = "10",
        directionRef = "381",
        order = null,
        aimedArrival = null,
        expectedArrival = expected,
        aimedDeparture = null,
        expectedDeparture = null,
        vehicleRef = vehicle,
        stopRef = stop,
        cancellation = false,
        predictionInaccurate = false,
        delaySeconds = null,
        arrivalStatus = null,
    )

    @Test
    fun `given a followed vehicle when the line names the same trip then that trip is pinned`() {
        val arrivals =
            listOf(
                row("60", riderStop, dueAtStop + 3.minutes),
                row("75", riderStop, dueAtStop + 22.minutes),
            )
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop + 3.minutes,
                followedVehicleRef = "60",
            )
        assertEquals("60", trip)
    }

    @Test
    fun `given a pinned trip when the hourly match points elsewhere then the pinned trip is kept`() {
        val arrivals =
            listOf(
                row("60", riderStop, dueAtStop + 3.minutes),
                row("75", riderStop, dueAtStop + 22.minutes),
            )
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop + 3.minutes,
                followedVehicleRef = "1200",
                pinned = "75",
            )
        assertEquals(
            "75",
            trip,
            "the followed bus must not change between polls",
        )
    }

    @Test
    fun `given no vehicle but a time at the rider's stop when it is looked up then that time pins the trip`() {
        val arrivals =
            listOf(
                row("60", riderStop, dueAtStop + 3.minutes),
                row("75", riderStop, dueAtStop + 22.minutes),
            )
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop,
                followedVehicleRef = "1200",
            )
        assertEquals("60", trip)
    }

    @Test
    fun `given a bus due minutes later when it is compared then it is another bus`() {
        val arrivals = listOf(row("75", riderStop, dueAtStop + 22.minutes))
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop,
                followedVehicleRef = "1200",
            )
        assertNull(trip)
    }

    @Test
    fun `given two clocks apart when they are compared then the tolerance separates the same bus from the next one`() {
        val arrivals =
            listOf(
                row(
                    "75",
                    riderStop,
                    dueAtStop + TRIP_MATCH_TOLERANCE_SECONDS.seconds,
                ),
            )
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop,
                followedVehicleRef = null,
            )
        assertEquals("75", trip)
    }

    @Test
    fun `given the line stops publishing the pinned trip when it is looked up then the pinned trip is kept`() {
        val arrivals = listOf(row("75", riderStop, dueAtStop + 22.minutes))
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = dueAtStop,
                followedVehicleRef = "60",
                pinned = "60",
            )
        assertEquals("60", trip)
    }

    @Test
    fun `given an empty feed when the trip is looked up then the pinned trip is kept`() {
        val trip =
            pinnedTrip(
                arrivals = emptyList(),
                followedStopRef = riderStop,
                followedAt = dueAtStop,
                followedVehicleRef = "60",
                pinned = "60",
            )
        assertEquals("60", trip)
    }

    @Test
    fun `given neither vehicle nor time when it is looked up then nothing is pinned`() {
        val arrivals = listOf(row("60", riderStop, dueAtStop + 3.minutes))
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = null,
                followedVehicleRef = null,
            )
        assertNull(trip)
    }

    @Test
    fun `given a blank vehicle when it is looked up then it does not match`() {
        val arrivals = listOf(row(" ", riderStop, dueAtStop + 3.minutes))
        val trip =
            pinnedTrip(
                arrivals = arrivals,
                followedStopRef = riderStop,
                followedAt = null,
                followedVehicleRef = " ",
            )
        assertNull(trip)
    }
}
