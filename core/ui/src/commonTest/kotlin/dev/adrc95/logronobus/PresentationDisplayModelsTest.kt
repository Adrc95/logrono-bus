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

import dev.adrc95.logronobus.core.testing.builder.departure
import dev.adrc95.logronobus.core.testing.builder.stop
import dev.adrc95.logronobus.core.ui.mapper.toDirectionRows
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PresentationDisplayModelsTest {
    @Test
    fun `given a departure row when it is built then it names where the bus goes`() {
        val row = departure().toDisplayModel()
        assertEquals(DepartureDestination.Towards("Valdegastea"), row.destination)
        assertEquals("4", row.lineCode)
        assertTrue(row.isTrackable)
    }

    @Test
    fun `given the operator's capitals when the row is painted then they are styled`() {
        val row = departure { withDisplayName("PALACIO DE CONGRESOS") }.toDisplayModel()
        assertEquals(DepartureDestination.Towards("Palacio de Congresos"), row.destination)
    }

    @Test
    fun `given a screen that knows better when the row is built then it carries that name`() {
        val row = departure().toDisplayModel(destinationLabel = "Madre de Dios")
        assertEquals(DepartureDestination.Towards("Madre de Dios"), row.destination)
    }

    @Test
    fun `given the ends of a direction when the stops are named then they read as first and last and not as a destination`() {
        val origin = departure().toDisplayModel(isDirectionOrigin = true)
        val end = departure().toDisplayModel(isDirectionEnd = true)
        val arrives = departure { withStartsHere(true) }.toDisplayModel()
        val terminates = departure { withTerminatesHere(true) }.toDisplayModel()
        assertEquals(DepartureDestination.StartsHere, origin.destination)
        assertEquals(DepartureDestination.TerminatesHere, end.destination)
        assertEquals(DepartureDestination.StartsHere, arrives.destination)
        assertEquals(DepartureDestination.TerminatesHere, terminates.destination)
    }

    @Test
    fun `given a headsign that repeats the stop's name when it is read then it means the end of the trip`() {
        val row = departure { withHeadsign("Vitoria") }.toDisplayModel(stopName = "Vitoria")
        assertEquals(DepartureDestination.TerminatesHere, row.destination)
    }

    @Test
    fun `given the route panel when its rows are built then they know the ends of the direction`() {
        val stops =
            listOf(
                stop { withStopRef("a") },
                stop { withStopRef("b") },
                stop { withStopRef("c") },
            )
        val rows =
            listOf(departure()).toDirectionRows(
                stop = stops.first(),
                lineLabel = "Madre de Dios",
                isDirectionOrigin = true,
                isDirectionEnd = false,
            )
        assertEquals(DepartureDestination.StartsHere, rows.single().destination)
    }
}
