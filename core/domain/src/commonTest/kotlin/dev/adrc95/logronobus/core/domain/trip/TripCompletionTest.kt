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

import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class TripCompletionTest {
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

    private fun busAt(
        latitude: Double,
        longitude: Double = -2.44,
    ) = BusLocation(
        latitude = latitude,
        longitude = longitude,
        modifiedTimestamp = Instant.parse("2026-10-18T12:00:00Z"),
        tripDuid = "trip-1",
    )

    @Test
    fun `given a bus standing at the terminus when the trip is checked then it has finished`() {
        assertTrue(stops.hasTripCompleted(busAt(42.464)))
    }

    @Test
    fun `given a bus past the terminus when the trip is checked then it has finished`() {
        assertTrue(stops.hasTripCompleted(busAt(42.465)))
    }

    @Test
    fun `given a bus on the last leg when the trip is checked then it has not finished`() {
        assertFalse(stops.hasTripCompleted(busAt(42.463)))
    }

    @Test
    fun `given a bus at the start of the route when the trip is checked then it has not finished`() {
        assertFalse(stops.hasTripCompleted(busAt(42.460)))
    }

    @Test
    fun `given a bus near the terminus on the wrong side when the trip is checked then it has not finished`() {
        assertFalse(stops.hasTripCompleted(busAt(42.4635, -2.442)))
    }

    @Test
    fun `given no bus or a route of one stop when the trip is checked then nothing is told`() {
        assertFalse(stops.hasTripCompleted(null))
        assertFalse(listOf(stop("A", 42.460)).hasTripCompleted(busAt(42.460)))
    }
}
