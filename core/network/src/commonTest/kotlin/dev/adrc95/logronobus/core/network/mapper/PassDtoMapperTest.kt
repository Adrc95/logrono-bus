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

import dev.adrc95.logronobus.core.network.builder.passDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PassDtoMapperTest {
    @Test
    fun given_a_pass_when_mapped_to_domain_then_the_aimed_times_are_read_as_instants() {
        val dto =
            passDto {
                withLineRef("6")
                withStopPointRef("152")
                withJourneyCode("J1")
                withOrder(11)
                withAimedArrivalTime("2026-10-18T12:00:00Z")
                withAimedDepartureTime("2026-10-18T12:00:00Z")
            }

        val result = dto.toDomain()

        assertEquals("6", result.lineRef)
        assertEquals("152", result.stopRef)
        assertEquals(11, result.order)
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), result.aimedArrival)
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), result.aimedDeparture)
    }

    @Test
    fun given_a_pass_when_mapped_to_domain_then_there_is_no_vehicle_and_no_expected_time() {
        val dto =
            passDto {
                withAimedArrivalTime("2026-10-18T12:00:00Z")
            }

        val result = dto.toDomain()

        assertNull(result.vehicleRef)
        assertNull(result.expectedArrival)
        assertNull(result.expectedDeparture)
    }

    @Test
    fun given_a_pass_with_a_zero_padded_stop_when_mapped_then_the_stop_has_no_leading_zeros() {
        val dto =
            passDto {
                withStopPointRef("000152")
            }

        assertEquals("152", dto.toDomain().stopRef)
    }
}
