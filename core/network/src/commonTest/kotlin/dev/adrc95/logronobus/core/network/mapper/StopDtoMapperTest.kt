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

import dev.adrc95.logronobus.core.network.builder.stopDto
import kotlin.test.Test
import kotlin.test.assertEquals

class StopDtoMapperTest {
    @Test
    fun given_a_stop_when_mapped_to_domain_then_it_carries_the_catalogue_data() {
        val dto =
            stopDto {
                withId(152)
                withName("Plaza de la iglesia El Cortijo")
                withLat(42.492503)
                withLng(-2.520566)
                withStatus(1)
                withLines(listOf(6))
            }

        val result = dto.toDomain(codes = mapOf(6 to "6"))

        assertEquals("152", result.stopRef)
        assertEquals("152", result.stopId)
        assertEquals("Plaza de la iglesia El Cortijo", result.longName)
        assertEquals("Plaza de la iglesia El Cortijo", result.shortName)
        assertEquals(42.492503, result.latitude)
        assertEquals(-2.520566, result.longitude)
    }

    @Test
    fun given_a_stop_with_internal_line_ids_when_mapped_then_the_lines_are_the_visible_codes() {
        val dto =
            stopDto {
                withId(157)
                withLines(listOf(31, 10, 4))
            }

        val result = dto.toDomain(codes = mapOf(31 to "B1", 10 to "10", 4 to "4"))

        assertEquals(listOf("B1", "10", "4"), result.routes)
    }

    @Test
    fun given_a_stop_with_an_unknown_line_id_when_mapped_then_the_id_is_kept() {
        val dto =
            stopDto {
                withId(157)
                withLines(listOf(99))
            }

        val result = dto.toDomain(codes = mapOf(31 to "B1"))

        assertEquals(listOf("99"), result.routes)
    }
}
