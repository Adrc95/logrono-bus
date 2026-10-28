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

import dev.adrc95.logronobus.core.network.dto.LatLngDto
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto
import dev.adrc95.logronobus.core.network.dto.LineStopsDto
import dev.adrc95.logronobus.core.network.dto.StopDto
import dev.adrc95.logronobus.core.network.mapper.directionHeadsigns
import dev.adrc95.logronobus.core.network.mapper.directionShapes
import dev.adrc95.logronobus.core.network.mapper.directionStops
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.mapper.visibleCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogronoMappingTest {
    private fun stop(
        id: Int,
        name: String,
        internalLines: List<Int> = listOf(6),
    ) = StopDto(
        id = id,
        name = name,
        lat = 42.4 + id / 1000.0,
        lng = -2.44 - id / 1000.0,
        status = 1,
        lines = internalLines,
    )

    private fun lineStop(
        id: Int,
        name: String,
        shape: List<LatLngDto> = emptyList(),
    ) = LineStopDto(id = id, name = name, lat = 42.4, lng = -2.44, status = 1, onwardShape = shape)

    @Test
    fun `given a catalogue name when its code is read then it is the prefix before the first dash`() {
        assertEquals("6", visibleCode("6-CENTRO-EL CORTIJO"))
        assertEquals("B1", visibleCode("B1-VAREA-LA CAVA"))
        assertEquals("10", visibleCode("10-EL ARCO-HOSPITAL SAN PEDRO"))
    }

    @Test
    fun `given a stop with internal ids when it is mapped then it carries the visible line codes`() {
        val mapped =
            stop(157, "Vitoria", internalLines = listOf(31, 10, 4))
                .toDomain(codes = mapOf(31 to "B1", 10 to "10", 4 to "4"))
        assertEquals("157", mapped.stopRef)
        assertEquals("157", mapped.stopId)
        assertEquals("Vitoria", mapped.longName)
        assertEquals(listOf("B1", "10", "4"), mapped.routes)
    }

    @Test
    fun `given an internal id the catalogue does not know when it is mapped then its number is kept`() {
        val mapped = stop(1, "X").toDomain(codes = emptyMap())
        assertEquals(listOf("6"), mapped.routes, "no code known yet: the raw id is the best label")
    }

    @Test
    fun `given a line when it is mapped then the route is keyed by its visible code`() {
        val route =
            LineDto(
                id = 31,
                name = "B1-VAREA-LA CAVA",
                color = "rgba(253,2,0,1)",
                status = 1,
                stops =
                    LineStopsDto(
                        asc = listOf(lineStop(1, "Artesanos"), lineStop(2, "La Cava")),
                        desc = listOf(lineStop(2, "La Cava"), lineStop(1, "Artesanos")),
                    ),
            ).toDomain()
        assertEquals("B1", route.shortName)
        assertEquals(
            "B1",
            route.timetableId,
            "the visible code is the key everything else joins on",
        )
        assertEquals("B1-VAREA-LA CAVA", route.longName)
        assertEquals("rgba(253,2,0,1)", route.color, "the line's real colour, as published")
    }

    @Test
    fun `given a line with two directions when its stops are mapped then each direction keeps its own list`() {
        val line =
            LineDto(
                id = 6,
                name = "6-CENTRO-EL CORTIJO",
                color = null,
                status = 1,
                stops =
                    LineStopsDto(
                        asc = listOf(lineStop(1, "Eliseo Pinedo"), lineStop(2, "Belena")),
                        desc = listOf(lineStop(2, "Belena"), lineStop(1, "Eliseo Pinedo")),
                    ),
            )
        val perDirection = line.directionStops()
        assertEquals(2, perDirection.size)
        assertEquals(listOf("1", "2"), perDirection[0].map { it.stopRef })
        assertEquals("Belena", perDirection[1].first().longName)
    }

    @Test
    fun `given a direction ending at a two-kerb terminus when it is mapped then the kerbside it sets down at is kept`() {
        val line =
            LineDto(
                id = 4,
                name = "4-PALACIO DE CONGRESOS-PRADOVIEJO",
                color = null,
                status = 1,
                stops =
                    LineStopsDto(
                        asc =
                            listOf(
                                lineStop(58, "Palacio de Congresos"),
                                lineStop(800, "Barrio Ballesteros"),
                            ),
                        desc =
                            listOf(
                                lineStop(800, "Barrio Ballesteros"),
                                LineStopDto(
                                    id = 212,
                                    name = "Palacio de Congresos",
                                    lat = 42.468484,
                                    lng = -2.430941,
                                    status = 1,
                                    onwardShape = emptyList(),
                                ),
                                LineStopDto(
                                    id = 58,
                                    name = "Palacio de Congresos",
                                    lat = 42.468303,
                                    lng = -2.431076,
                                    status = 1,
                                    onwardShape = emptyList(),
                                ),
                            ),
                    ),
            )
        val perDirection = line.directionStops()

        assertEquals(listOf("800", "212"), perDirection[1].map { it.stopRef })
        assertEquals("Palacio de Congresos", perDirection[1].last().longName)

        assertEquals(listOf("58", "800"), perDirection[0].map { it.stopRef })
    }

    @Test
    fun `given two stops with the same name a street apart when they are mapped then both are kept`() {
        val line =
            LineDto(
                id = 33,
                name = "B3-LAS NORIAS-EL CORTIJO",
                color = null,
                status = 1,
                stops =
                    LineStopsDto(
                        asc =
                            listOf(
                                LineStopDto(id = 816, name = "Las Norias", lat = 42.4600, lng = -2.4400, status = 1, onwardShape = emptyList()),
                                LineStopDto(id = 228, name = "Las Norias", lat = 42.4600, lng = -2.4374, status = 1, onwardShape = emptyList()),
                            ),
                        desc = listOf(lineStop(228, "Las Norias")),
                    ),
            )
        assertEquals(listOf("816", "228"), line.directionStops()[0].map { it.stopRef })
    }

    @Test
    fun `given a line when its headsigns are read then each one is its direction's last stop`() {
        val line =
            LineDto(
                id = 6,
                name = "6-CENTRO-EL CORTIJO",
                color = null,
                status = 1,
                stops =
                    LineStopsDto(
                        asc =
                            listOf(
                                lineStop(1, "Eliseo Pinedo"),
                                lineStop(2, "Plaza de la iglesia El Cortijo"),
                            ),
                        desc =
                            listOf(
                                lineStop(3, "Plaza de la iglesia El Cortijo"),
                                lineStop(4, "Eliseo Pinedo"),
                            ),
                    ),
            )

        assertEquals(
            listOf("Plaza de la iglesia El Cortijo", "Eliseo Pinedo"),
            line.directionHeadsigns(),
        )
    }

    @Test
    fun `given a direction with onward shapes when it is mapped then they are concatenated in order`() {
        val line =
            LineDto(
                id = 6,
                name = "6",
                color = null,
                status = 1,
                stops =
                    LineStopsDto(
                        asc =
                            listOf(
                                lineStop(1, "A", shape = listOf(LatLngDto(1.0, 1.0), LatLngDto(1.0, 1.1))),
                                lineStop(2, "B", shape = listOf(LatLngDto(1.0, 1.1), LatLngDto(1.0, 1.2))),
                            ),
                        desc = listOf(lineStop(2, "B"), lineStop(1, "A")),
                    ),
            )
        val shapes = line.directionShapes()
        assertEquals(2, shapes.size)
        assertEquals(
            4,
            shapes[0].size,
            "one shared boundary point ends up twice — harmless for a polyline",
        )
        assertEquals(1.0, shapes[0].first().lat, 1e-9)
    }

    @Test
    fun `given an empty direction when it is mapped then it has no stops and no shape`() {
        val line =
            LineDto(
                id = 9,
                name = "9",
                color = null,
                status = 1,
                stops = LineStopsDto(asc = emptyList(), desc = emptyList()),
            )
        assertTrue(line.directionStops().isEmpty())
        assertTrue(line.directionHeadsigns().isEmpty())
        assertTrue(line.directionShapes().isEmpty())
    }
}
