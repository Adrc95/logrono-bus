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

import dev.adrc95.logronobus.core.domain.departure.terminatesHere
import dev.adrc95.logronobus.core.domain.trip.directionIndexOf
import dev.adrc95.logronobus.core.domain.trip.inDirection
import dev.adrc95.logronobus.core.domain.trip.toDeparture
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.dto.ArrivalDto
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto
import dev.adrc95.logronobus.core.network.dto.LineStopsDto
import dev.adrc95.logronobus.core.network.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class DepartureDirectionTest {
    private fun stop(
        id: Int,
        name: String,
    ) = LineStopDto(
        id = id,
        name = name,
        lat = 0.0,
        lng = 0.0,
        status = 1,
        onwardShape = emptyList(),
    )

    private val line =
        LineDto(
            id = 10,
            name = "10-EL ARCO-HOSPITAL SAN PEDRO",
            color = "rgba(15,220,252,1)",
            status = 1,
            stops =
                LineStopsDto(
                    asc =
                        listOf(
                            stop(202, "Manuel de Falla"),
                            stop(58, "Palacio de Congresos"),
                            stop(30, "Artesanos"),
                        ),
                    desc =
                        listOf(
                            stop(30, "Artesanos"),
                            stop(58, "Palacio de Congresos"),
                            stop(202, "Manuel de Falla"),
                        ),
                ),
        )

    private val lineSequences =
        listOf(
            StopSequence(
                line.stops.asc.map { it.id },
                line.stops.asc
                    .lastOrNull()
                    ?.name,
            ),
            StopSequence(
                line.stops.desc.map { it.id },
                line.stops.desc
                    .lastOrNull()
                    ?.name,
            ),
        )

    private fun arrival(
        line: String,
        order: Int,
        vehicle: String = "48",
        dirRef: String = "381",
    ) = dev.adrc95.logronobus.core.network.dto.ArrivalDto(
        lineRef = line,
        directionRef = dirRef,
        journeyCode = "",
        vehicleRef = vehicle,
        stopPointRef = "202",
        order = order,
        aimedArrivalTime = "2026-10-15T23:00:00+02:00",
        expectedArrivalTime = "2026-10-15T23:05:00+02:00",
        aimedDepartureTime = "2026-10-15T23:00:00+02:00",
        expectedDepartureTime = "2026-10-15T23:05:00+02:00",
        arrivalStatus = "SCHEDULED",
        departureStatus = "SCHEDULED",
        cancellation = false,
        predictionInaccurate = false,
        delaySeconds = 0,
    )

    private val now = Instant.parse("2026-10-15T21:00:00Z")

    @Test
    fun `given a bus with predictions when its direction is asked then the end of the line it heads for answers`() {
        assertEquals(
            0,
            directionIndexOf(
                lineSequences,
                stopId = 202,
                order = 1,
            ),
        )
        assertEquals(
            1,
            directionIndexOf(
                lineSequences,
                stopId = 202,
                order = 3,
            ),
        )
        assertNull(
            directionIndexOf(
                lineSequences,
                stopId = 202,
                order = 2,
            ),
        )
    }

    @Test
    fun `given a bus that ends at the rider's stop when directions are resolved then it is not shown as going there`() {
        val departure =
            arrival(
                "10",
                order = 3,
            ).toDomain().toDeparture(headsign = "Manuel de Falla", now = now, windowMinutes = 90)!!
        assertTrue(
            terminatesHere(departure, stopName = "Manuel de Falla"),
            "the bus finishes its trip at this stop: saying so beats repeating the stop's name",
        )
    }

    @Test
    fun `given a bus heading elsewhere when directions are resolved then it keeps its destination`() {
        val departure =
            arrival(
                "10",
                order = 1,
            ).toDomain().toDeparture(headsign = "Artesanos", now = now, windowMinutes = 90)!!
        assertEquals("Artesanos", departure.displayName)
        assertTrue(!terminatesHere(departure, stopName = "Manuel de Falla"))
    }

    @Test
    fun `given no direction in mind when buses are filtered then none is dropped`() {
        val departures =
            inDirection(
                departures =
                    listOf(
                        departure("10", "Artesanos", 0),
                        departure("10", "Manuel de Falla", 1),
                    ),
                direction = null,
            )
        assertEquals(2, departures.size)
    }

    @Test
    fun `given a direction is open when buses are filtered then only that direction's buses remain`() {
        val departures =
            inDirection(
                departures =
                    listOf(
                        departure("10", "Artesanos", 0),
                        departure("10", "Manuel de Falla", 1),
                    ),
                direction = 1,
            )
        assertEquals(listOf("Manuel de Falla"), departures.map { it.displayName })
    }

    @Test
    fun `given a bus with no tellable direction when buses are filtered then it is kept`() {
        val departures =
            inDirection(
                departures = listOf(departure("10", "?", null)),
                direction = 1,
            )
        assertEquals(1, departures.size, "a bus you can see out of the window must not vanish")
    }

    private fun departure(
        line: String,
        headsign: String,
        directionIndex: Int?,
    ) = DepartureTime(
        displayName = headsign,
        timetableId = line,
        departTimestamp = Instant.parse("2026-10-15T23:05:00+02:00"),
        delaySeconds = 0,
        tripId = "48",
        vehicleId = "48",
        headsign = headsign,
        directionIndex = directionIndex,
    )
}
