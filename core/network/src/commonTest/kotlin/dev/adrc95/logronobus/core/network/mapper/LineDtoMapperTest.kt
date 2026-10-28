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

package dev.adrc95.logronobus.core.network.mapper

import dev.adrc95.logronobus.core.network.builder.latLngDto
import dev.adrc95.logronobus.core.network.builder.lineDto
import dev.adrc95.logronobus.core.network.builder.lineStopDto
import dev.adrc95.logronobus.core.network.builder.lineStopsDto
import kotlin.test.Test
import kotlin.test.assertEquals

class LineDtoMapperTest {
    @Test
    fun given_a_line_when_mapped_to_domain_then_it_is_keyed_by_the_code_the_rider_reads() {
        val dto =
            lineDto {
                withId(6)
                withName("6-CENTRO-EL CORTIJO")
                withColor("rgba(146,76,72,1)")
                withStatus(1)
            }

        val result = dto.toDomain()

        assertEquals("6", result.timetableId)
        assertEquals("6", result.shortName)
        assertEquals("6-CENTRO-EL CORTIJO", result.longName)
        assertEquals("rgba(146,76,72,1)", result.color)
    }

    @Test
    fun given_a_line_with_a_night_name_when_mapped_then_the_code_keeps_its_letter() {
        val dto =
            lineDto {
                withId(31)
                withName("B1-VAREA-LA CAVA")
            }

        assertEquals("B1", dto.toDomain().timetableId)
    }

    @Test
    fun given_a_line_when_reading_its_directions_then_each_side_keeps_its_stops() {
        val dto =
            lineDto {
                withStops(
                    lineStopsDto {
                        withAsc(
                            listOf(
                                lineStopDto {
                                    withId(10)
                                    withName("Eliseo Pinedo")
                                },
                            ),
                        )
                        withDesc(
                            listOf(
                                lineStopDto {
                                    withId(152)
                                    withName("El Cortijo")
                                },
                            ),
                        )
                    },
                )
            }

        val directions = dto.directionStops()

        assertEquals(2, directions.size)
        assertEquals(listOf("Eliseo Pinedo"), directions[0].map { it.longName })
        assertEquals(listOf("El Cortijo"), directions[1].map { it.longName })
    }

    @Test
    fun given_a_line_when_reading_its_headsigns_then_each_one_is_the_last_stop_of_its_side() {
        val dto =
            lineDto {
                withStops(
                    lineStopsDto {
                        withAsc(
                            listOf(
                                lineStopDto {
                                    withId(10)
                                    withName("Eliseo Pinedo")
                                },
                                lineStopDto {
                                    withId(152)
                                    withName("Plaza de la iglesia El Cortijo")
                                },
                            ),
                        )
                        withDesc(
                            listOf(
                                lineStopDto {
                                    withId(152)
                                    withName("Plaza de la iglesia El Cortijo")
                                },
                            ),
                        )
                    },
                )
            }

        assertEquals(
            listOf("Plaza de la iglesia El Cortijo", "Plaza de la iglesia El Cortijo"),
            dto.directionHeadsigns(),
        )
    }

    @Test
    fun given_a_line_when_reading_its_shape_then_the_onward_points_are_the_ones_of_its_stops() {
        val dto =
            lineDto {
                withStops(
                    lineStopsDto {
                        withAsc(
                            listOf(
                                lineStopDto {
                                    withId(10)
                                    withName("Eliseo Pinedo")
                                    withOnwardShape(
                                        listOf(
                                            latLngDto {
                                                withLat(42.46)
                                                withLng(-2.44)
                                            },
                                        ),
                                    )
                                },
                            ),
                        )
                    },
                )
            }

        val shape = dto.directionShapes().single()

        assertEquals(1, shape.size)
        assertEquals(42.46, shape.single().lat)
        assertEquals(-2.44, shape.single().lon)
    }
}
