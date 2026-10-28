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

import dev.adrc95.logronobus.core.domain.trip.fleetClock
import dev.adrc95.logronobus.core.domain.trip.usableActivities
import dev.adrc95.logronobus.core.model.FleetActivity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class FleetActivityFilterTest {
    private val now = Instant.parse("2026-10-20T22:00:00Z")

    private fun activity(
        lineRef: String = "9",
        vehicleRef: String = "1200",
        nextStopRef: String? = "801",
        recordedAt: Instant? = now,
    ) = FleetActivity(
        vehicleRef = vehicleRef,
        lineRef = lineRef,
        directionRef = "Ida",
        publishedLineName = null,
        latitude = 0.0,
        longitude = 0.0,
        recordedAt = recordedAt,
        delaySeconds = null,
        predictionInaccurate = false,
        nextStopRef = nextStopRef,
        nextStopExpectedAt = null,
        nextStopAimedAt = null,
    )

    private fun usable(vararg activities: FleetActivity) =
        usableActivities(
            activities = activities.toList(),
            servingLines = setOf(9, 10),
            visibleLines = setOf(9, 10),
            clock = now,
            freshMinutes = 20L,
        )

    @Test
    fun `given a report older than the window when the fleet is read then the bus does not count`() {
        val fresh = activity(recordedAt = now)
        val stale = activity(recordedAt = Instant.parse("2026-10-20T21:39:00Z"))

        assertEquals(listOf("1200"), usable(fresh, stale).map { it.vehicleRef })
    }

    @Test
    fun `given a line nobody publishes when the fleet is read then it is not a bus`() {
        assertEquals(emptyList(), usable(activity(lineRef = "77")))
    }

    @Test
    fun `given a report without vehicle or next stop when it is read then it says nothing`() {
        assertEquals(emptyList(), usable(activity(vehicleRef = "  ")))
        assertEquals(emptyList(), usable(activity(nextStopRef = null)))
        assertEquals(emptyList(), usable(activity(nextStopRef = "no es un número")))
        assertEquals(emptyList(), usable(activity(recordedAt = null)))
    }

    @Test
    fun `given both clocks when the fleet is judged then the feed's own wins`() {
        val server = Instant.parse("2026-10-20T22:30:00Z")

        assertEquals(server, fleetClock(server.toEpochMilliseconds(), now))
        assertEquals(now, fleetClock(null, now), "without a server clock, the phone's is all there is")
    }

    @Test
    fun `given a report at the edge of the window when it is read then it still counts`() {
        val justInside = activity(recordedAt = Instant.parse("2026-10-20T21:40:00Z"))
        val justOutside = activity(recordedAt = Instant.parse("2026-10-20T21:39:00Z"))

        assertTrue(usable(justInside).isNotEmpty())
        assertTrue(usable(justOutside).isEmpty())
    }
}
