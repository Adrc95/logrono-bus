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

package dev.adrc95.logronobus.feature.stops.mapper

import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.feature.stops.mapper.toDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StopSheetDisplayModelTest {
    @Test
    fun `given a stop sheet when it is built then it prefers one row per line and falls back to the next departures`() {
        val stop = stop { withStopRef("360") }
        val sheet =
            stop.toDisplayModel(
                lineStarts =
                    listOf(
                        departure { withTimetableId("4") },
                        departure { withTimetableId("B1") },
                    ),
                departures = listOf(departure { withTimetableId("9") }),
                lineLabels = mapOf("4" to "Valdegastea"),
                routeOrigins = emptyMap(),
                routeColors = emptyMap(),
                isLoading = true,
                notLive = false,
                isFavourite = true,
            )!!

        assertEquals(listOf("4", "B1"), sheet.rows.map { it.lineCode })
        assertEquals(DepartureDestination.Towards("Valdegastea"), sheet.rows[0].destination)
        assertEquals(DepartureDestination.Towards("Valdegastea"), sheet.rows[1].destination)
        assertEquals(false, sheet.isLoading)
        assertEquals("Plaza de la iglesia El Cortijo", sheet.title)
        assertEquals("360", sheet.stopId)
        assertEquals("360", sheet.stopRef)

        val fallback =
            stop.toDisplayModel(
                lineStarts = emptyList(),
                departures = listOf(departure { withTimetableId("9") }),
                lineLabels = emptyMap(),
                routeOrigins = emptyMap(),
                routeColors = emptyMap(),
                isLoading = true,
                notLive = false,
                isFavourite = false,
            )!!

        assertEquals(listOf("9"), fallback.rows.map { it.lineCode })
        assertEquals(true, fallback.isLoading)
    }

    @Test
    fun `given no open stop when the sheet is built then there is none`() {
        assertNull(
            (null as Stop?).toDisplayModel(
                lineStarts = emptyList(),
                departures = emptyList(),
                lineLabels = emptyMap(),
                routeOrigins = emptyMap(),
                routeColors = emptyMap(),
                isLoading = false,
                notLive = false,
                isFavourite = false,
            ),
        )
    }
}
