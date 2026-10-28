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

import dev.adrc95.logronobus.core.domain.trip.headsign
import dev.adrc95.logronobus.core.domain.trip.remember
import dev.adrc95.logronobus.core.model.StopSequence
import dev.adrc95.logronobus.core.network.datasource.LogronoCatalogueDataSource
import dev.adrc95.logronobus.core.network.dto.ApiLinesResponse
import dev.adrc95.logronobus.core.network.dto.ApiLinesResult
import dev.adrc95.logronobus.core.network.dto.ApiStopsResponse
import dev.adrc95.logronobus.core.network.dto.LatLngDto
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto
import dev.adrc95.logronobus.core.network.dto.LineStopsDto
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DirectionsIntegrationTest {
    private suspend fun sequencesOf(lines: List<LineDto>): Map<String, List<StopSequence>> = LogronoCatalogueDataSource(FakeCatalogueService(lines = lines)).sequences()

    private fun sequence(
        vararg ids: Int,
        terminal: String,
    ) = StopSequence(ids.toList(), terminal)

    private val sequences =
        mapOf(
            "6" to
                listOf(
                    sequence(10, 20, 152, 30, terminal = "Plaza de la iglesia El Cortijo"),
                    sequence(30, 152, 20, 10, terminal = "Eliseo Pinedo"),
                ),
        )

    @Test
    fun `given a matching sequence when the headsign is read then its terminal is the destination`() {
        assertEquals(
            "Plaza de la iglesia El Cortijo",
            headsign(
                "6",
                stopId = 152,
                order = 3,
                directionRef = "363",
                sequences = sequences,
                learned = emptyMap(),
            ),
        )
        assertEquals(
            "Eliseo Pinedo",
            headsign(
                "6",
                stopId = 152,
                order = 2,
                directionRef = "77",
                sequences = sequences,
                learned = emptyMap(),
            ),
        )
    }

    @Test
    fun `given no match and nothing learned when the headsign is read then it is empty rather than guessed`() {
        assertNull(
            headsign(
                "6",
                stopId = 152,
                order = 1,
                directionRef = "999",
                sequences = sequences,
                learned = emptyMap(),
            ),
            "a short turning matches no sequence: say nothing rather than the wrong destination",
        )
        assertNull(
            headsign(
                "9",
                stopId = 152,
                order = 3,
                directionRef = "1",
                sequences = sequences,
                learned = emptyMap(),
            ),
        )
    }

    @Test
    fun `given a learned direction when a trip starts mid-route then it still resolves`() {
        val learned = mapOf("6" to mapOf("363" to "Plaza de la iglesia El Cortijo"))
        assertEquals(
            "Plaza de la iglesia El Cortijo",
            headsign(
                "6",
                stopId = 152,
                order = 1,
                directionRef = "363",
                sequences = sequences,
                learned = learned,
            ),
        )
    }

    @Test
    fun `given a resolved headsign when it is read again then it is remembered for its directionRef`() {
        val afterFirst =
            remember(
                emptyMap(),
                "6",
                "363",
                "El Cortijo",
            )
        assertEquals(
            "El Cortijo",
            headsign(
                "6",
                152,
                order = 99,
                directionRef = "363",
                sequences = sequences,
                learned = afterFirst,
            ),
        )

        assertNull(
            headsign(
                "6",
                152,
                order = 99,
                directionRef = "364",
                sequences = sequences,
                learned = afterFirst,
            ),
        )
    }

    @Test
    fun `given an ambiguous match when the headsign is read then it resolves to nothing`() {
        val ambiguous =
            mapOf(
                "4" to
                    listOf(
                        sequence(5, 61, 7, terminal = "Alzheimer"),
                        sequence(9, 61, 7, terminal = "Pradoviejo"),
                    ),
            )
        assertNull(
            headsign(
                "4",
                stopId = 61,
                order = 2,
                directionRef = "x",
                sequences = ambiguous,
                learned = emptyMap(),
            ),
        )
    }

    @Test
    fun `given the catalogue when the sequences are read then each code has its two directions`() =
        runTest {
            val lines =
                listOf(
                    LineDto(
                        id = 6,
                        name = "6-CENTRO-EL CORTIJO",
                        color = null,
                        status = 1,
                        stops =
                            LineStopsDto(
                                asc =
                                    listOf(
                                        lineStop(10, "Eliseo Pinedo"),
                                        lineStop(152, "Belena"),
                                        lineStop(30, "El Cortijo"),
                                    ),
                                desc =
                                    listOf(
                                        lineStop(30, "El Cortijo"),
                                        lineStop(152, "Belena"),
                                        lineStop(10, "Eliseo Pinedo"),
                                    ),
                            ),
                    ),
                )
            val built = sequencesOf(lines)
            assertEquals(listOf("6"), built.keys.toList())
            assertEquals(2, built.getValue("6").size)
            assertEquals("El Cortijo", built.getValue("6").first().terminal)
            assertEquals("Eliseo Pinedo", built.getValue("6").last().terminal)
            assertEquals(listOf(10, 152, 30), built.getValue("6").first().ids)
        }

    @Test
    fun `given a line with two directions when they are mapped then each keeps its own terminal`() =
        runTest {
            val built =
                sequencesOf(
                    listOf(
                        LineDto(
                            id = 7,
                            name = "7-EL ARCO-POLÍGONO CANTABRIA",
                            color = null,
                            status = 1,
                            stops =
                                LineStopsDto(
                                    asc = listOf(lineStop(1, "Labradores"), lineStop(2, "Naval")),
                                    desc = listOf(lineStop(2, "Naval"), lineStop(3, "Enrique Granados")),
                                ),
                        ),
                    ),
                )
            assertEquals(listOf("Naval", "Enrique Granados"), built.getValue("7").map { it.terminal })
            assertTrue(built.getValue("7").none { it.ids.isEmpty() })
        }

    private fun lineStop(
        id: Int,
        name: String = "stop $id",
    ) = LineStopDto(
        id = id,
        name = name,
        lat = 0.0,
        lng = 0.0,
        status = 1,
        onwardShape = listOf(LatLngDto(0.0, 0.0)),
    )
}
