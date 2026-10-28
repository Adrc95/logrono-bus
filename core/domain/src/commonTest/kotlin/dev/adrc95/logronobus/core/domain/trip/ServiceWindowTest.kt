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
import dev.adrc95.logronobus.core.model.StopPrediction
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ServiceWindowTest {
    private val now = Instant.parse("2026-10-27T22:05:00Z")

    private val terminus =
        Stop(
            stopRef = "156",
            stopId = "156",
            longName = "Pradoviejo",
            shortName = "Pradoviejo",
            latitude = 42.4500,
            longitude = -2.4400,
        )

    private fun bus(
        nextStopIn: Duration?,
        latitude: Double = 42.4500,
        longitude: Double = -2.4400,
    ) = BusLocation(
        latitude = latitude,
        longitude = longitude,
        modifiedTimestamp = now,
        tripDuid = "7290",
        nextStops =
            nextStopIn?.let {
                listOf(StopPrediction(stopRef = "156", stopSequence = 0, departureTimestamp = now + it))
            },
    )

    private fun stops() = listOf(terminus)

    @Test
    fun `given a bus with its next stop an hour away and far from it when it is checked then it is not serving`() {
        // When
        val serving = bus(53.minutes, latitude = 42.4700, longitude = -2.4700).isServingWithin(30, now, stops(), waitMinutes = 60)

        // Then
        assertFalse(serving, "An hour to the next stop, two kilometres from it, is a bus leaving service")
    }

    @Test
    fun `given a bus parked at a stop for over an hour when it is checked then it is not serving`() {
        // When
        val serving = bus(70.minutes).isServingWithin(30, now, stops(), waitMinutes = 60)

        // Then
        assertFalse(serving, "Parked for over an hour is not a bus about to leave")
    }

    @Test
    fun `given a bus waiting at its terminus for a late departure when it is checked then it is serving`() {
        // When
        val serving = bus(25.minutes).isServingWithin(30, now, stops(), waitMinutes = 60)

        // Then
        assertTrue(serving, "Sitting on the stop it will leave from is a bus about to start")
    }

    @Test
    fun `given a bus a few minutes from its next stop when it is checked then it is serving`() {
        // When
        val serving =
            bus(4.minutes, latitude = 42.4700, longitude = -2.4700).isServingWithin(30, now, stops())

        // Then
        assertTrue(serving)
    }

    @Test
    fun `given a bus without a next stop when it is checked then it is left alone`() {
        // When
        val serving = bus(null).isServingWithin(30, now, stops())

        // Then
        assertTrue(serving, "Nothing to judge it by, so it is not dropped")
    }
}
