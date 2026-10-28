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
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.RiderStopState
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.TrackingViewInput
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.feature.tracking.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class TrackingFollowChoiceTest {
    private val line = "10"
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
            stop("S2", -2.4380),
            stop("S3", -2.4360),
            stop("S4", -2.4340),
        )

    private fun departure(
        vehicleId: String? = null,
        tripId: String? = null,
    ) = DepartureTime(
        displayName = headsign,
        timetableId = line,
        tripId = tripId,
        vehicleId = vehicleId,
        headsign = headsign,
        directionIndex = 0,
    )

    private fun bus(
        longitude: Double,
        vehicleRef: String,
        nextStopRef: String,
        lineCode: String = line,
    ) = BusLocation(
        latitude = 42.46,
        longitude = longitude,
        bearing = null,
        modifiedTimestamp = Instant.parse("2026-10-19T14:00:00Z"),
        tripDuid = vehicleRef,
        vehicleId = vehicleRef,
        timetableId = lineCode,
        headsign = headsign,
        nextStopRef = nextStopRef,
        nextStops = null,
        shapeId = null,
    )

    private fun modelTheRiderSees(
        positions: List<BusLocation>,
        at: String = "S2",
        departure: DepartureTime = departure(),
        preferred: String? = null,
    ) = TrackingViewInput(
        routeStops = listOf(stops),
        directionHeadsigns = listOf(headsign),
        departure = departure,
        tripId = departure.tripId,
        stopRef = at,
        positions = positions,
        preferredVehicleId = preferred,
    ).display()

    @Test
    fun `given a bus that already went past the stop when the bus to follow is chosen then it is not the one offered`() {
        // Given
        val past = bus(longitude = -2.4350, vehicleRef = "1702", nextStopRef = "S3")
        val coming = bus(longitude = -2.4385, vehicleRef = "1306", nextStopRef = "S2")

        // When
        val display = modelTheRiderSees(listOf(past, coming))

        // Then
        assertEquals(
            "1306",
            display.trackedBus?.vehicleId,
            "Following a bus that has left says it is coming",
        )
        assertEquals("1306", display.trackedTripId)
    }

    @Test
    fun `given the departure's own bus when it is chosen then it is the one followed`() {
        // Given
        val mine = bus(longitude = -2.4370, vehicleRef = "1702", nextStopRef = "S3")
        val other = bus(longitude = -2.4390, vehicleRef = "1306", nextStopRef = "S2")

        // When
        val display =
            modelTheRiderSees(listOf(mine, other), departure = departure(vehicleId = "1702"))

        // Then
        assertEquals(listOf("1702"), display.positions.map { it.vehicleId })
        assertEquals("1702", display.trackedTripId)
    }

    @Test
    fun `given a bus of another line when the fallback is looked for then it is never taken`() {
        // Given
        val otherLine =
            bus(longitude = -2.4385, vehicleRef = "9999", nextStopRef = "S2", lineCode = "4")

        // When
        val display = modelTheRiderSees(listOf(otherLine))

        // Then
        assertNull(
            display.trackedBus,
            "The rider asked for line 10: a bus of line 4 is not their ride",
        )
    }

    @Test
    fun `given a bus already followed when it goes past the rider's stop then it is kept`() {
        // Given
        val mine = bus(longitude = -2.4350, vehicleRef = "1702", nextStopRef = "S4")
        val behind = bus(longitude = -2.4390, vehicleRef = "1306", nextStopRef = "S2")

        // When
        val display = modelTheRiderSees(listOf(mine, behind), preferred = "1702")

        // Then
        assertEquals("1702", display.trackedBus?.vehicleId, "The bus being watched stays the bus")
        assertEquals(RiderStopState.Passed, display.riderStopState, "…and the card says it went by")
    }

    @Test
    fun `given a followed bus that leaves the feed when the model is built then it is not silently swapped for another`() {
        // Given
        val stranger = bus(longitude = -2.4390, vehicleRef = "1306", nextStopRef = "S2")

        // When
        val display = modelTheRiderSees(listOf(stranger), preferred = "1702")

        // Then
        assertNull(
            display.trackedBus,
            "Better to say this bus is not reporting than to call another bus the same ride",
        )
    }

    @Test
    fun `given a bus past the rider's stop when the card is built then it says so`() {
        val gone = bus(longitude = -2.4358, vehicleRef = "1702", nextStopRef = "S4")
        val coming = bus(longitude = -2.4390, vehicleRef = "1306", nextStopRef = "S2")
        assertEquals(
            RiderStopState.Approaching,
            modelTheRiderSees(listOf(coming)).riderStopState,
        )
        assertEquals(
            RiderStopState.Passed,
            modelTheRiderSees(listOf(gone), preferred = "1702").riderStopState,
            "A bus past the stop is not arriving",
        )
    }

    @Test
    fun `given a handover to a bus not reporting yet when the card is built then it does not claim it has gone by`() {
        // Given
        val old = bus(longitude = -2.4350, vehicleRef = "1702", nextStopRef = "S4")

        // When
        val display =
            modelTheRiderSees(
                listOf(old),
                departure = departure(vehicleId = "1900", tripId = "1900"),
            )

        // Then
        assertEquals(
            RiderStopState.Unknown,
            display.riderStopState,
            "A guessed bus may say it is coming, never that the rider's ride has gone",
        )
    }
}

private fun TrackingViewInput.display() =
    GetTrackingViewUseCase()(this).toDisplayModel(
        routeColors = emptyMap(),
        trackedShape = RouteLineDisplayModel(),
        routeShapes = emptyList(),
    )
