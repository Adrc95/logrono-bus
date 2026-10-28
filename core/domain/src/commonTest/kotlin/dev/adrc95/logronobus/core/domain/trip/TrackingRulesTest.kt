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

import dev.adrc95.logronobus.core.domain.geo.isStandingStill
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class TrackingRulesTest {
    private val timeline =
        listOf(
            stop("A", lon = -2.4400),
            stop("B", lon = -2.4380),
            stop("C", lon = -2.4360),
        )

    private fun stop(
        ref: String,
        lat: Double = 42.46,
        lon: Double = -2.44,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = "Stop $ref",
        shortName = ref,
        latitude = lat,
        longitude = lon,
    )

    private fun bus(
        ref: String = "1",
        lat: Double,
        lon: Double,
        nextStopRef: String? = null,
    ) = BusLocation(
        latitude = lat,
        longitude = lon,
        modifiedTimestamp = Instant.parse("2026-10-25T10:00:00Z"),
        tripDuid = ref,
        vehicleId = ref,
        timetableId = "B1",
        nextStopRef = nextStopRef,
    )

    @Test
    fun `given a direction when the role of each stop is asked then the ends answer`() {
        // When
        val roles = listOf(0, 1, 2).map { index -> stopRoleAt(index, 3) }

        // Then
        assertEquals(listOf(StopRole.ORIGIN, StopRole.MIDDLE, StopRole.END), roles)
    }

    @Test
    fun `given a direction of one stop when the role is asked then it is not an end`() {
        // When
        val role = stopRoleAt(0, 1)

        // Then
        assertEquals(StopRole.MIDDLE, role)
    }

    @Test
    fun `given a bus behind the waiting stop when it is compared then it is still ahead`() {
        // Given
        val stops =
            listOf(
                stop("a", lat = 42.4600, lon = -2.4400),
                stop("b", lat = 42.4610, lon = -2.4420),
                stop("c", lat = 42.4620, lon = -2.4440),
            )
        val behind = bus(lat = 42.4605, lon = -2.4405)

        // When
        val ahead = behind.isStillAheadOf(stops, stop("c"))

        // Then
        assertTrue(ahead)
    }

    @Test
    fun `given a bus past the waiting stop when it is compared then it is not ahead`() {
        // Given
        val stops =
            listOf(
                stop("a", lat = 42.4600, lon = -2.4400),
                stop("b", lat = 42.4610, lon = -2.4420),
                stop("c", lat = 42.4620, lon = -2.4440),
            )
        val past = bus(lat = 42.4630, lon = -2.4460)

        // When
        val ahead = past.isStillAheadOf(stops, stop("b"))

        // Then
        assertFalse(ahead)
    }

    @Test
    fun `given a one stop jump and a small move when the index settles then the previous index is kept`() {
        // When
        val settled = settleBusIndex(rawIndex = 5, previousIndex = 4, metresPastStop = 10.0)

        // Then
        assertEquals(4, settled)
    }

    @Test
    fun `given a one stop jump and the bus already clear of the stop when the index settles then it moves on`() {
        // When
        val settled = settleBusIndex(rawIndex = 5, previousIndex = 4, metresPastStop = 30.0)

        // Then
        assertEquals(
            5,
            settled,
            "Twenty metres is enough to accept the stop has been left, so the label must not linger",
        )
    }

    @Test
    fun `given a one stop jump but a long move when the index settles then the new index is kept`() {
        // When
        val settled = settleBusIndex(rawIndex = 5, previousIndex = 4, metresPastStop = 200.0)

        // Then
        assertEquals(5, settled)
    }

    @Test
    fun `given a stop of gps jitter behind the bus when the index settles then it does not walk back`() {
        // When
        val settled = settleBusIndex(rawIndex = 3, previousIndex = 4, metresPastStop = 30.0)

        // Then
        assertEquals(
            4,
            settled,
            "Thirty metres is not the bus turning round, so the stop it has passed stays passed",
        )
    }

    @Test
    fun `given the bus truly going back along the route when the index settles then it follows it`() {
        // When
        val settled = settleBusIndex(rawIndex = 3, previousIndex = 4, metresPastStop = 400.0)

        // Then
        assertEquals(3, settled)
    }

    @Test
    fun `given the feed lagging behind a bus that has clearly left the stop when it is checked then it reads as passed`() {
        // Given
        val bus = bus(lat = 42.4600, lon = -2.4320)

        // When
        val ahead =
            bus.isStillAheadOf(
                timeline,
                stop("A", lon = -2.4400),
                heading = 1,
            )

        // Then
        assertFalse(
            ahead,
            "The feed still says stop B, but the bus is well past A and heading for C",
        )
    }

    @Test
    fun `given the feed lagging but the bus still at the stop when it is checked then it reads as arriving`() {
        // Given
        val bus = bus(lat = 42.4600, lon = -2.4401)

        // When
        val ahead =
            bus.isStillAheadOf(
                timeline,
                stop("A", lon = -2.4400),
                heading = 0,
            )

        // Then
        assertTrue(ahead, "Ten metres from the stop is not having passed it")
    }

    @Test
    fun `given a feed behind a bus that moved between reports when it is checked then it is not standing still`() {
        // Given
        val before = bus(lat = 42.4600, lon = -2.4400)
        val after = bus(lat = 42.4700, lon = -2.4500)

        // When
        val standing = after.isStandingStill(listOf(before))

        // Then
        assertFalse(standing)
    }

    @Test
    fun `given a bus reported twice at the same place when it is checked then it is standing still`() {
        // Given
        val before = bus(lat = 42.4600, lon = -2.4400)
        val after = bus(lat = 42.4600, lon = -2.4400)

        // When
        val standing = after.isStandingStill(listOf(before))

        // Then
        assertTrue(standing)
    }

    @Test
    fun `given the feed naming the next stop when it is read then it beats the geometry`() {
        // Given
        val tracked = bus(lat = 42.4600, lon = -2.4390, nextStopRef = "C")

        // When
        val next = nextStopRefOf(tracked, timeline)

        // Then
        assertEquals(
            "C",
            next,
            "The bus is between A and B, but the operator says it is heading for C, and it is the operator's bus",
        )
    }

    @Test
    fun `given no next stop in the feed when it is read then the geometry answers`() {
        // Given
        val tracked = bus(lat = 42.4600, lon = -2.4390, nextStopRef = null)

        // When
        val next = nextStopRefOf(tracked, timeline)

        // Then
        assertEquals("B", next)
    }

    @Test
    fun `given a next stop outside the timeline when it is read then the geometry answers`() {
        // Given
        val tracked = bus(lat = 42.4600, lon = -2.4390, nextStopRef = "Z")

        // When
        val next = nextStopRefOf(tracked, timeline)

        // Then
        assertEquals("B", next, "A stop of another direction is not this timeline's next stop")
    }
}
