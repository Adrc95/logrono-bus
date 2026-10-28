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

package dev.adrc95.logronobus.feature.tracking.mapper

import dev.adrc95.logronobus.core.domain.usecase.GetTrackingViewUseCase
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.TrackingViewInput
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.feature.tracking.mapper.toDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class TrackingAtStopTest {
    private val headsign = "Hospital San Pedro"

    private fun stop(
        ref: String,
        longitude: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = ref,
        shortName = ref,
        latitude = 42.46,
        longitude = longitude,
    )

    private val stops =
        listOf(
            stop("S1", -2.4400),
            stop("S2", -2.4370),
            stop("S3", -2.4340),
        )

    private fun bus(longitude: Double) =
        BusLocation(
            latitude = 42.46,
            longitude = longitude,
            modifiedTimestamp = Instant.parse("2026-10-19T14:00:00Z"),
            tripDuid = "1702",
            vehicleId = "1702",
            timetableId = "10",
            headsign = headsign,
            nextStopRef = "S2",
        )

    private fun model(
        busLongitude: Double,
        movedSinceLastReport: Boolean = false,
        seenBefore: Boolean = true,
    ) = TrackingViewInput(
        routeStops = listOf(stops),
        directionHeadsigns = listOf(headsign),
        departure = null,
        tripId = null,
        stopRef = "S3",
        positions = listOf(bus(busLongitude)),
        previousPositions =
            if (seenBefore) {
                listOf(bus(if (movedSinceLastReport) busLongitude - 0.002 else busLongitude))
            } else {
                emptyList()
            },
    )

    @Test
    fun `given a bus standing at the kerb when the model is built then it is at that stop`() {
        // Given
        val input = model(busLongitude = -2.4340)

        // When
        val display = input.display()

        // Then
        assertEquals("S3", display.atStopRef)
        assertTrue(display.atYourStop)
    }

    @Test
    fun `given a bus driving past the stop when the model is built then it is only on its way`() {
        // Given
        val input = model(busLongitude = -2.4340, movedSinceLastReport = true)

        // When
        val display = input.display()

        // Then
        assertNull(display.atStopRef)
        assertFalse(display.atYourStop)
    }

    @Test
    fun `given the feed still naming the stop just left when the model is built then the kerb decides`() {
        // Given
        val input = model(busLongitude = -2.4340)

        // When
        val display = input.display()

        // Then
        assertEquals("S3", display.atStopRef)
    }

    @Test
    fun `given a bus nobody has seen before when the model is built then it is only on its way`() {
        // Given
        val input = model(busLongitude = -2.4340, seenBefore = false)

        // When
        val display = input.display()

        // Then
        assertNull(display.atStopRef)
        assertFalse(display.atYourStop)
    }

    @Test
    fun `given a bus standing at another stop when the model is built then it is not at the rider's`() {
        // Given
        val input = model(busLongitude = -2.4370)

        // When
        val display = input.display()

        // Then
        assertEquals("S2", display.atStopRef)
        assertFalse(display.atYourStop)
    }

    @Test
    fun `given a bus standing halfway between two stops when the model is built then it is at neither`() {
        // Given
        val input = model(busLongitude = -2.4355)

        // When
        val display = input.display()

        // Then
        assertNull(display.atStopRef)
        assertFalse(display.atYourStop)
    }
}

private fun TrackingViewInput.display() =
    GetTrackingViewUseCase()(this).toDisplayModel(
        routeColors = emptyMap(),
        trackedShape = RouteLineDisplayModel(),
        routeShapes = emptyList(),
    )
