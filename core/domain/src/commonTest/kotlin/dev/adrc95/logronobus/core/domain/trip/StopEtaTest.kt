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
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class StopEtaTest {
    private fun stop(
        ref: String,
        longitude: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref.takeLast(6),
        longName = ref,
        shortName = ref,
        latitude = 53.27,
        longitude = longitude,
    )

    private val stops =
        listOf(
            stop("A", -9.10),
            stop("B", -9.08),
            stop("C", -9.06),
            stop("D", -9.04),
            stop("E", -9.02),
            stop("F", -9.00),
        )
    private val now = Instant.parse("2026-09-06T17:00:00Z")

    private val zone = TimeZone.UTC

    private fun inMinutes(minutes: Int) = now + minutes.minutes

    private fun busAt(vararg ahead: Pair<String, Instant?>) =
        BusLocation(
            latitude = 53.27,
            longitude = -9.11,
            modifiedTimestamp = Instant.parse("2026-09-06T17:00:00Z"),
            tripDuid = "trip-1",
            nextStops =
                ahead.mapIndexed { i, (ref, time) ->
                    StopPrediction(stopRef = ref, stopSequence = i, departureTimestamp = time)
                },
        )

    @Test
    fun `given a bus with predictions when the stops ahead are mapped then each one carries its clock time`() {
        val etas =
            stops.stopEtasFor(
                busAt("C" to inMinutes(5), "D" to inMinutes(12)),
                now = now,
                zone = zone,
            )
        assertEquals("17:05", etas.getValue("C").label)
        assertEquals("17:12", etas.getValue("D").label)
        assertTrue(etas.getValue("C").upcoming)
    }

    @Test
    fun `given a stop about to be reached when it is mapped then its time is still shown`() {
        val etas = stops.stopEtasFor(busAt("C" to inMinutes(0)), now = now, zone = zone)
        assertEquals("17:00", etas.getValue("C").label)
    }

    @Test
    fun `given a prediction past the hour when the label is built then it rolls over correctly`() {
        val etas = stops.stopEtasFor(busAt("C" to inMinutes(65)), now = now, zone = zone)
        assertEquals("18:05", etas.getValue("C").label, "17:00 + 65 min is 18:05, not 17:65")
    }

    @Test
    fun `given many stops ahead when they are mapped then only the next few are labelled`() {
        val bus =
            busAt(
                "A" to inMinutes(1),
                "B" to inMinutes(3),
                "C" to inMinutes(6),
                "D" to inMinutes(9),
            )
        val etas = stops.stopEtasFor(bus, labelledAhead = 3, now = now, zone = zone)
        assertEquals(
            listOf("A", "B", "C"),
            etas.filterValues { it.label.isNotBlank() }.keys.sorted(),
        )
        assertTrue(
            etas["D"] == null || etas.getValue("D").label.isBlank(),
            "The fourth stop ahead should be plain",
        )
    }

    @Test
    fun `given the tracked stop far ahead when it is mapped then it keeps its time`() {
        val bus =
            busAt(
                "A" to inMinutes(1),
                "B" to inMinutes(3),
                "C" to inMinutes(6),
                "D" to inMinutes(20),
            )
        val etas =
            stops.stopEtasFor(bus, trackedStopRef = "D", labelledAhead = 2, now = now, zone = zone)
        assertEquals(
            "17:20",
            etas.getValue("D").label,
            "The stop you are waiting at must always show its time",
        )
    }

    @Test
    fun `given stops behind the bus when they are mapped then they are marked passed rather than unknown`() {
        val etas =
            stops.stopEtasFor(
                busAt("C" to inMinutes(5), "D" to inMinutes(12)),
                now = now,
                zone = zone,
            )
        listOf("A", "B").forEach { ref ->
            val eta = etas[ref]
            assertTrue(eta != null, "Stop $ref should be marked as passed")
            assertTrue(!eta.upcoming, "Stop $ref should not be upcoming")
            assertEquals("", eta.label, "A passed stop has no prediction to show")
        }
    }

    @Test
    fun `given a stop without a prediction when it is mapped then it is left plain`() {
        val etas =
            stops.stopEtasFor(busAt("C" to null, "D" to inMinutes(12)), now = now, zone = zone)
        assertNull(etas["C"], "An ahead-but-untimed stop should not be labelled or muted")
        assertTrue(etas.getValue("D").upcoming)
    }

    @Test
    fun `given no bus in the feed when the stops are mapped then nothing is annotated`() {
        assertTrue(stops.stopEtasFor(null, zone = zone).isEmpty())
    }

    @Test
    fun `given a prediction a few seconds behind when it is mapped then it is not muted`() {
        val etas =
            stops.stopEtasFor(busAt("C" to now - 30.seconds), now = now, zone = zone)
        assertEquals(
            "16:59",
            etas.getValue("C").label,
            "30s behind is plain latency, not a passed stop",
        )
        assertTrue(etas.getValue("C").upcoming)
    }

    @Test
    fun `given the line's own feed when the stops ahead are mapped then they carry its times`() {
        val predictions =
            mapOf(
                "A" to inMinutes(1),
                "B" to inMinutes(3),
                "C" to inMinutes(6),
                "D" to inMinutes(9),
            )
        val etas =
            stops.stopEtasFor(
                busAt(),
                predictions = predictions,
                labelledAhead = 3,
                now = now,
                zone = zone,
            )
        assertEquals("17:01", etas.getValue("A").label)
        assertEquals("17:03", etas.getValue("B").label)
        assertEquals("17:06", etas.getValue("C").label)
        assertTrue(etas.getValue("D").label.isBlank(), "only the next few are labelled")
    }

    @Test
    fun `given a time for a stop the bus has left when it is mapped then it is dropped`() {
        val past =
            BusLocation(
                latitude = 53.27,
                longitude = -9.07,
                modifiedTimestamp = Instant.parse("2026-09-06T17:00:00Z"),
                tripDuid = "trip-1",
            )
        val predictions = mapOf("A" to inMinutes(1), "B" to inMinutes(3), "C" to inMinutes(6))
        val etas = stops.stopEtasFor(past, predictions = predictions, now = now, zone = zone)
        assertTrue(etas.getValue("A").label.isBlank(), "A is behind the bus")
        assertTrue(etas.getValue("B").label.isBlank(), "B is behind the bus")
        assertEquals("17:06", etas.getValue("C").label)
    }

    @Test
    fun `given a prediction genuinely behind now when it is mapped then it is muted like a passed stop`() {
        val etas = stops.stopEtasFor(busAt("C" to inMinutes(-3)), now = now, zone = zone)
        val eta = etas["C"]
        assertTrue(eta != null, "Still in the ahead-list, so still worth a marker")
        assertTrue(!eta.upcoming, "3 minutes exceeds the grace window, so it should read as passed")
        assertEquals("", eta.label, "A stale clock time is worse than none")
    }

    @Test
    fun `given several overdue stops at once when they are mapped then all are muted`() {
        val bus = busAt("A" to inMinutes(-4), "B" to inMinutes(-3), "C" to inMinutes(2))
        val etas = stops.stopEtasFor(bus, now = now, zone = zone)
        assertEquals("", etas.getValue("A").label)
        assertEquals("", etas.getValue("B").label)
        assertEquals(
            "17:02",
            etas.getValue("C").label,
            "The one stop still ahead keeps its real time",
        )
    }

    @Test
    fun `given the line's time and the bus's own time for the next stop when they disagree then the bus wins`() {
        // Given
        val operatorTimes =
            mapOf(
                "C" to inMinutes(11),
                "D" to inMinutes(19),
            )

        // When
        val etas =
            stops.stopEtasFor(
                busAt("C" to inMinutes(1)),
                predictions = operatorTimes,
                now = now,
                zone = zone,
            )

        // Then
        assertEquals(
            "17:01",
            etas.getValue("C").label,
            "The stop the bus is heading to is timed from where the bus actually is",
        )
        assertEquals(
            "17:19",
            etas.getValue("D").label,
            "The following stops keep the line's own estimate, which is not shifted to guess",
        )
    }

    @Test
    fun `given a bus running late against the line's times when the stops are mapped then nothing is invented`() {
        // Given
        val operatorTimes =
            mapOf(
                "C" to inMinutes(11),
                "D" to inMinutes(19),
                "E" to inMinutes(27),
            )

        // When
        val etas =
            stops.stopEtasFor(
                busAt("C" to inMinutes(1)),
                predictions = operatorTimes,
                now = now,
                zone = zone,
            )

        // Then
        assertEquals(
            "17:19",
            etas.getValue("D").label,
            "Only the stop the bus is heading to has a live time; the rest stay as the line says",
        )
        assertEquals(
            "17:27",
            etas.getValue("E").label,
            "A guessed shift moved every stop once, and it was wrong as often as right",
        )
    }
}
