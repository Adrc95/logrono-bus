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
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class DirectionFromMovementTest {
    @Test
    fun `given a bus running towards the end of the first direction when it is read then it goes that way`() {
        // When
        val direction =
            directions.directionFromMovement(
                previous = bus(LONGITUDE_WEST),
                current = bus(LONGITUDE_EAST, atMillis = MINUTE_MILLIS),
            )

        // Then
        assertEquals(0, direction)
    }

    @Test
    fun `given a bus running back towards the start when it is read then it goes the other way`() {
        // When
        val direction =
            directions.directionFromMovement(
                previous = bus(LONGITUDE_EAST),
                current = bus(LONGITUDE_WEST, atMillis = MINUTE_MILLIS),
            )

        // Then
        assertEquals(1, direction)
    }

    @Test
    fun `given a bus standing still when it is read then no direction is made up`() {
        // When
        val direction =
            directions.directionFromMovement(
                previous = bus(LONGITUDE_WEST),
                current = bus(LONGITUDE_WEST, atMillis = MINUTE_MILLIS),
            )

        // Then
        assertNull(direction)
    }

    @Test
    fun `given a jump smaller than a stop when it is read then no direction is made up`() {
        // When
        val direction =
            directions.directionFromMovement(
                previous = bus(LONGITUDE_WEST),
                current = bus(LONGITUDE_WEST + 0.0002, atMillis = MINUTE_MILLIS),
            )

        // Then
        assertNull(direction)
    }

    @Test
    fun `given a gps jump a bus could never drive when it is read then no direction is made up`() {
        // When
        val direction =
            directions.directionFromMovement(
                previous = bus(LONGITUDE_WEST),
                current = bus(LONGITUDE_EAST, atMillis = 5_000L),
            )

        // Then
        assertNull(direction, "Seven hundred metres in five seconds is a bad fix, not a bus")
    }

    private fun bus(
        longitude: Double,
        atMillis: Long = 0L,
    ) = BusLocation(
        latitude = LATITUDE,
        longitude = longitude,
        modifiedTimestamp = Instant.fromEpochMilliseconds(atMillis),
        tripDuid = "bus",
    )

    private fun stopsAt(vararg longitudes: Double): List<Stop> =
        longitudes.mapIndexed { index, longitude ->
            Stop(
                stopRef = "stop-$index-$longitude",
                stopId = index.toString(),
                longName = "Parada $index",
                shortName = "Parada $index",
                latitude = LATITUDE,
                longitude = longitude,
            )
        }

    private val directions =
        listOf(
            stopsAt(-2.4801, -2.4753, -2.4711, -2.4640),
            stopsAt(-2.4645, -2.4676, -2.4715, -2.4770),
        )
}

private const val LATITUDE = 42.4612
private const val LONGITUDE_WEST = -2.4788
private const val LONGITUDE_EAST = -2.4700
private const val MINUTE_MILLIS = 60_000L
