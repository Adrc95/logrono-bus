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
import dev.adrc95.logronobus.core.model.StopPrediction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class BusesForDirectionTest {
    private val outbound = setOf("out1", "out2", "out3", "shared")
    private val inbound = setOf("in1", "in2", "in3", "shared")
    private val refs = listOf(outbound, inbound)
    private val headsigns = listOf("Varea", "El Cortijo")

    private fun bus(
        trip: String,
        headsign: String? = null,
        ahead: List<String> = emptyList(),
        nextStop: String? = null,
        direction: Int? = null,
    ) = BusLocation(
        latitude = 53.27,
        longitude = -9.05,
        modifiedTimestamp = Instant.fromEpochMilliseconds(0),
        tripDuid = trip,
        headsign = headsign,
        nextStopRef = nextStop,
        nextStops =
            ahead
                .takeIf { it.isNotEmpty() }
                ?.mapIndexed { i, ref -> StopPrediction(stopRef = ref, stopSequence = i) },
        directionIndex = direction,
    )

    @Test
    fun `given a bus with predictions when its direction is resolved then the stops ahead decide it`() {
        assertEquals(
            0,
            bus("t", ahead = listOf("out2", "out3"))
                .directionOf(refs, headsigns),
        )
        assertEquals(
            1,
            bus("t", ahead = listOf("in1", "in2"))
                .directionOf(refs, headsigns),
        )
    }

    @Test
    fun `given a variant headsign when the bus is filtered then it is no longer hidden`() {
        val shortWorking = bus("t", headsign = "Universidad solo", ahead = listOf("out2", "out3"))
        assertEquals(
            0,
            shortWorking.directionOf(
                refs,
                headsigns,
            ),
        )
        assertTrue(
            shortWorking in
                listOf(shortWorking).forDirection(
                    refs,
                    headsigns,
                    direction = 0,
                ),
        )
        assertTrue(
            shortWorking !in
                listOf(shortWorking).forDirection(
                    refs,
                    headsigns,
                    direction = 1,
                ),
        )
    }

    @Test
    fun `given a bus without predictions when its direction is resolved then the headsign answers`() {
        assertEquals(
            1,
            bus("t", headsign = "El Cortijo")
                .directionOf(
                    refs,
                    headsigns,
                ),
        )
    }

    @Test
    fun `given a bus on a shared stop only when it is resolved then it is not forced into a direction`() {
        assertNull(
            bus("t", ahead = listOf("shared"))
                .directionOf(
                    refs,
                    headsigns,
                ),
        )
    }

    @Test
    fun `given an undecidable bus when the direction is open then it is shown in every direction`() {
        val unknown = bus("t", headsign = "Cocheras")
        assertTrue(
            unknown in
                listOf(unknown)
                    .forDirection(
                        refs,
                        headsigns,
                        direction = 0,
                    ),
        )
        assertTrue(
            unknown in
                listOf(unknown)
                    .forDirection(
                        refs,
                        headsigns,
                        direction = 1,
                    ),
        )
    }

    @Test
    fun `given a bus on a shared stop when the feed states its direction then the feed wins`() {
        val bus = bus("t", ahead = listOf("shared"), direction = 1)
        assertEquals(
            1,
            bus.directionOf(
                refs,
                headsigns,
            ),
        )
        assertTrue(
            bus !in
                listOf(bus).forDirection(
                    refs,
                    headsigns,
                    direction = 0,
                ),
        )
    }

    @Test
    fun `given no feed direction when the stop is shared then the bus stays undecided`() {
        assertNull(
            bus("t", ahead = listOf("shared"), direction = null)
                .directionOf(
                    refs,
                    headsigns,
                ),
        )
    }

    @Test
    fun `given no prediction list when the direction is resolved then nextStopRef answers`() {
        assertEquals(
            0,
            bus("t", nextStop = "out1")
                .directionOf(
                    refs,
                    headsigns,
                ),
        )
    }

    @Test
    fun `given a route with one direction when it is filtered then nothing is dropped`() {
        val buses = listOf(bus("a", headsign = "x"), bus("b", headsign = "y"))
        assertEquals(
            buses,
            buses
                .forDirection(
                    listOf(outbound),
                    listOf("Varea"),
                    direction = 0,
                ),
        )
    }

    @Test
    fun `given a bus the feed places one way and its movement the other when it is filtered then the movement wins`() {
        // Given
        val buses = listOf(bus("t", headsign = "Varea", direction = 0))

        // When
        val goingBack =
            buses.forDirection(
                refs,
                headsigns,
                direction = 1,
                overrides = mapOf("t" to 1),
            )
        val asTheFeedSays =
            buses.forDirection(
                refs,
                headsigns,
                direction = 1,
            )

        // Then
        assertEquals(buses, goingBack)
        assertTrue(asTheFeedSays.isEmpty())
    }
}
