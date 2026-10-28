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

import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopPrediction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class UpcomingStopsForTest {
    private fun stop(ref: String) =
        Stop(
            stopRef = ref,
            stopId = ref.takeLast(6),
            longName = ref,
            shortName = ref,
            latitude = 53.27,
            longitude = -9.05,
        )

    private val stops = listOf(stop("A"), stop("B"), stop("C"), stop("D"))
    private val now = Instant.parse("2026-09-06T17:00:00Z")

    private fun inMinutes(minutes: Int) = now + minutes.minutes

    private fun busAt(vararg ahead: Pair<String, Instant?>) =
        BusLocation(
            latitude = 53.27,
            longitude = -9.05,
            modifiedTimestamp = Instant.parse("2026-09-06T17:00:00Z"),
            tripDuid = "trip-1",
            nextStops =
                ahead.mapIndexed { i, (ref, time) ->
                    StopPrediction(stopRef = ref, stopSequence = i, departureTimestamp = time)
                },
        )

    @Test
    fun `given predictions ahead when the upcoming stops are listed then they keep their minutes nearest first`() {
        val result =
            busAt("A" to inMinutes(2), "B" to inMinutes(9))
                .upcomingStops(stops, now)
        assertEquals(
            listOf("A" to 2, "B" to 9),
            result.map { (s, m) -> s.stopRef to m },
        )
    }

    @Test
    fun `given a prediction under a minute away when the minutes are read then due means the last minute`() {
        val result =
            busAt(
                "A" to now + 20.seconds,
                "B" to now + 50.seconds,
                "C" to now + 150.seconds,
            ).upcomingStops(stops, now)
        assertEquals(
            listOf(0, 0, 2),
            result.map { it.second },
            "20s and 50s are due, 2m30s is 2 min because minutes are counted whole",
        )
    }

    @Test
    fun `given a prediction a few seconds behind when the list is built then it is not dropped`() {
        val result =
            busAt("A" to now - 30.seconds, "B" to inMinutes(5)).upcomingStops(
                stops,
                now,
            )
        assertEquals(listOf("A", "B"), result.map { it.first.stopRef })
    }

    @Test
    fun `given a prediction genuinely behind now when the list is built then it is dropped`() {
        val result =
            busAt("A" to inMinutes(-3), "B" to inMinutes(5))
                .upcomingStops(stops, now)
        assertEquals(
            listOf("B"),
            result.map { it.first.stopRef },
            "A's moment is well past, so it should not appear at all",
        )
    }

    @Test
    fun `given several overdue stops when the list is built then all of them are dropped`() {
        val bus =
            busAt(
                "A" to inMinutes(-4),
                "B" to inMinutes(-3),
                "C" to inMinutes(0),
                "D" to inMinutes(2),
            )
        val result = bus.upcomingStops(stops, now)
        assertEquals(listOf("C", "D"), result.map { it.first.stopRef })
    }

    @Test
    fun `given a prediction exactly at now when the list is built then it is still shown`() {
        val result = busAt("A" to inMinutes(0)).upcomingStops(stops, now)
        assertEquals(listOf("A" to 0), result.map { (s, m) -> s.stopRef to m })
    }

    @Test
    fun `given an untimed stop when the list is built then it appears with no minutes`() {
        val result = busAt("A" to null).upcomingStops(stops, now)
        assertEquals(1, result.size)
        assertNull(result.first().second)
    }

    @Test
    fun `given a stop ref outside the trip when the list is built then it is skipped`() {
        val result =
            busAt("Z" to inMinutes(1), "A" to inMinutes(2))
                .upcomingStops(stops, now)
        assertEquals(listOf("A"), result.map { it.first.stopRef })
    }

    @Test
    fun `given more stops than the limit when the list is built then it is capped`() {
        val bus =
            busAt(
                "A" to inMinutes(1),
                "B" to inMinutes(2),
                "C" to inMinutes(3),
                "D" to inMinutes(4),
            )
        assertEquals(2, bus.upcomingStops(stops, now, limit = 2).size)
        assertTrue(bus.upcomingStops(stops, now).size <= 4, "Default limit is 4")
    }
}
