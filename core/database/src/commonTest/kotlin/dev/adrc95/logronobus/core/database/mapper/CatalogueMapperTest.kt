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

package dev.adrc95.logronobus.core.database.mapper

import dev.adrc95.logronobus.core.database.builder.catalogueLineEntity
import dev.adrc95.logronobus.core.database.builder.catalogueStopEntity
import dev.adrc95.logronobus.core.testing.builder.route
import dev.adrc95.logronobus.core.testing.builder.stop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogueMapperTest {
    @Test
    fun given_a_stop_when_it_is_mapped_to_a_row_then_it_keeps_its_id_name_and_position() {
        val result =
            stop {
                withStopRef("152")
                withStopId("152")
                withLongName("Plaza de la iglesia El Cortijo")
                withLatitude(42.492503)
                withLongitude(-2.520566)
            }.toEntity()

        assertEquals(152, result.id)
        assertEquals("Plaza de la iglesia El Cortijo", result.name)
        assertEquals(42.492503, result.lat)
        assertEquals(-2.520566, result.lng)
    }

    @Test
    fun given_a_stored_stop_row_when_it_is_mapped_back_then_it_becomes_a_stop_with_its_line_codes() {
        val result =
            catalogueStopEntity {
                withId(152)
                withName("Plaza de la iglesia El Cortijo")
                withLat(42.492503)
                withLng(-2.520566)
            }.toStop(routes = listOf("6", "10", "B1"))

        assertEquals("152", result.stopRef)
        assertEquals("152", result.stopId)
        assertEquals("Plaza de la iglesia El Cortijo", result.longName)
        assertEquals(listOf("6", "10", "B1"), result.routes)
        assertNull(result.direction)
    }

    @Test
    fun given_a_route_when_it_is_mapped_to_a_row_then_it_keeps_its_id_name_and_colour() {
        val result =
            route {
                withTimetableId("6")
                withLongName("6-CENTRO-EL CORTIJO")
                withColor("rgba(146,76,72,1)")
            }.toEntity()

        assertEquals(6, result.id)
        assertEquals("6-CENTRO-EL CORTIJO", result.name)
        assertEquals("rgba(146,76,72,1)", result.colour)
    }

    @Test
    fun given_a_stored_route_row_when_it_is_mapped_back_then_it_becomes_a_route() {
        val result =
            catalogueLineEntity {
                withId(6)
                withName("6-CENTRO-EL CORTIJO")
                withColour("rgba(146,76,72,1)")
            }.toRoute()

        assertEquals("6", result.timetableId)
        assertEquals("6-CENTRO-EL CORTIJO", result.longName)
        assertEquals("rgba(146,76,72,1)", result.color)
    }

    @Test
    fun given_a_stored_night_row_when_it_is_mapped_back_then_the_code_keeps_the_b_the_rider_reads() {
        val result =
            catalogueLineEntity {
                withId(31)
                withCode("B1")
                withName("B1-VAREA-LA CAVA")
            }.toRoute()

        assertEquals("B1", result.timetableId)
        assertEquals("B1", result.shortName)
    }
}
