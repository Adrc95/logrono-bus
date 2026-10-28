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

import dev.adrc95.logronobus.core.network.builder.arrivalDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ArrivalDtoMapperTest {
    @Test
    fun given_an_arrival_when_mapped_to_domain_then_the_times_are_read_as_instants() {
        val dto =
            arrivalDto {
                withLineRef("6")
                withDirectionRef("363")
                withAimedArrivalTime("2026-10-18T12:00:00Z")
                withExpectedArrivalTime("2026-10-18T12:01:00Z")
                withAimedDepartureTime("2026-10-18T12:00:00Z")
                withExpectedDepartureTime("2026-10-18T12:01:00Z")
            }

        val result = dto.toDomain()

        assertEquals("6", result.lineRef)
        assertEquals("363", result.directionRef)
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), result.aimedArrival)
        assertEquals(Instant.parse("2026-10-18T12:01:00Z"), result.expectedArrival)
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), result.aimedDeparture)
        assertEquals(Instant.parse("2026-10-18T12:01:00Z"), result.expectedDeparture)
    }

    @Test
    fun given_an_arrival_with_a_zero_padded_stop_when_mapped_then_the_stop_has_no_leading_zeros() {
        val dto =
            arrivalDto {
                withStopPointRef("000152")
            }

        assertEquals("152", dto.toDomain().stopRef)
    }

    @Test
    fun given_an_arrival_without_a_vehicle_when_mapped_then_the_vehicle_is_null() {
        val dto =
            arrivalDto {
                withVehicleRef("")
            }

        assertNull(dto.toDomain().vehicleRef)
    }

    @Test
    fun given_an_arrival_without_a_status_when_mapped_then_the_status_is_null() {
        val dto =
            arrivalDto {
                withArrivalStatus("")
            }

        assertNull(dto.toDomain().arrivalStatus)
    }

    @Test
    fun given_an_arrival_with_an_unreadable_time_when_mapped_then_the_time_is_null() {
        val dto =
            arrivalDto {
                withAimedArrivalTime("soon")
            }

        assertNull(dto.toDomain().aimedArrival)
    }
}
