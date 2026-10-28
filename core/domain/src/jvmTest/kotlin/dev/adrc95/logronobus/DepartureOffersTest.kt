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

import dev.adrc95.logronobus.core.domain.trip.nextPerLine
import dev.adrc95.logronobus.core.domain.trip.offersTonight
import dev.adrc95.logronobus.core.domain.trip.servedDirections
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopSequence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class DepartureOffersTest {
    private fun row(
        line: String,
        at: Instant,
    ) = DepartureTime(displayName = line, timetableId = line, departTimestamp = at)

    @Test
    fun `given a night line when it is not running then it is not offered`() {
        val night = setOf("B1", "B2")
        val running = setOf("B2")

        assertFalse(offersTonight("B1", night, running), "B1 is not out tonight")
        assertTrue(offersTonight("B2", night, running), "B2 is out tonight")
        assertTrue(offersTonight("9", night, running), "a day line is always offered")
    }

    @Test
    fun `given a stop served in one direction when the other is open then it is not offered`() {
        val sequences =
            listOf(
                StopSequence(ids = listOf(1, 2), terminal = "A"),
                StopSequence(ids = listOf(2, 3), terminal = "B"),
            )

        assertEquals(listOf(0), servedDirections(sequences, stopId = 1, direction = 0))
        assertEquals(
            emptyList<Int?>(),
            servedDirections(sequences, stopId = 1, direction = 1),
            "the line does not call at that stop in this direction",
        )
    }

    @Test
    fun `given a stop without geometry when the offers are built then the overall schedule is kept`() {
        assertEquals(listOf<Int?>(null), servedDirections(emptyList(), stopId = 7, direction = null))
    }

    @Test
    fun `given several lines when the offers are built then each keeps its next departure`() {
        val rows =
            listOf(
                row("9", Instant.parse("2026-10-20T22:30:00Z")),
                row("B1", Instant.parse("2026-10-20T23:00:00Z")),
                row("9", Instant.parse("2026-10-20T22:00:00Z")),
            )

        assertEquals(
            listOf(Instant.parse("2026-10-20T22:00:00Z"), Instant.parse("2026-10-20T23:00:00Z")),
            nextPerLine(rows).map { it.departTimestamp },
        )
    }
}
