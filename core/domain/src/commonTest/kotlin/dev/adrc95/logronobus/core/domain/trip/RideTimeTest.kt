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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.model.Stop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RideTimeTest {
    private fun stop(
        ref: String,
        latitude: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = ref,
        shortName = ref,
        latitude = latitude,
        longitude = -2.44,
    )

    private val stops = listOf(stop("A", 42.460), stop("B", 42.462), stop("C", 42.464))

    @Test
    fun `given the origin of the direction when its ride time is read then it is zero`() {
        assertEquals(0, stops.minutesFromStartTo("A"))
    }

    @Test
    fun `given a ride already done when the time is read then it has grown`() {
        val first = stops.minutesFromStartTo("B")
        val second = stops.minutesFromStartTo("C")
        assertEquals(2, first)
        assertEquals(3, second)
    }

    @Test
    fun `given a stop outside the direction when its time is read then nothing is told`() {
        assertNull(stops.minutesFromStartTo("Z"))
    }
}
