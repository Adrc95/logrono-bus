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

import dev.adrc95.logronobus.core.network.NetworkConstants.NO_LINE
import dev.adrc95.logronobus.core.network.builder.activityDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ActivityDtoMapperTest {
    @Test
    fun given_an_activity_when_mapped_to_domain_then_it_carries_the_vehicle_and_the_line() {
        val dto =
            activityDto {
                withVehicleRef("9693")
                withLineRef("4")
                withDirectionRef("direction-4-1")
                withPublishedLineName("Valdegastea")
                withLatitude(42.46)
                withLongitude(-2.44)
                withLocationRecordedAtTime("2026-10-18T12:00:00Z")
            }

        val result = dto.toDomain()

        assertEquals("9693", result.vehicleRef)
        assertEquals("4", result.lineRef)
        assertEquals("direction-4-1", result.directionRef)
        assertEquals("Valdegastea", result.publishedLineName)
        assertEquals(42.46, result.latitude)
        assertEquals(-2.44, result.longitude)
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), result.recordedAt)
    }

    @Test
    fun given_an_activity_with_a_broken_line_name_when_mapped_then_the_name_is_null() {
        val dto =
            activityDto {
                withPublishedLineName(NO_LINE)
            }

        assertNull(dto.toDomain().publishedLineName)
    }

    @Test
    fun given_an_activity_with_a_zero_padded_stop_when_mapped_then_the_stop_has_no_leading_zeros() {
        val dto =
            activityDto {
                withNextStopRef("000152")
            }

        assertEquals("152", dto.toDomain().nextStopRef)
    }

    @Test
    fun given_an_activity_with_a_local_next_stop_time_when_mapped_then_it_is_read_as_an_instant() {
        val dto =
            activityDto {
                withNextStopExpectedArrivalTime("2026-10-18T12:05:00")
            }

        assertEquals(
            Instant.parse("2026-10-18T10:05:00Z"),
            dto.toDomain().nextStopExpectedAt,
        )
    }

    @Test
    fun given_an_activity_with_a_next_stop_time_wearing_a_z_when_mapped_then_it_is_read_as_local() {
        val dto =
            activityDto {
                withNextStopExpectedArrivalTime("2026-10-24T05:14:00Z")
            }
        assertEquals(
            Instant.parse("2026-10-24T03:14:00Z"),
            dto.toDomain().nextStopExpectedAt,
        )
    }
}
