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

package dev.adrc95.logronobus

import dev.adrc95.logronobus.core.domain.trip.toDeparture
import dev.adrc95.logronobus.core.network.dto.ArrivalDto
import dev.adrc95.logronobus.core.network.mapper.toDomain
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class LogronoArrivalsTest {
    private val now = Instant.parse("2026-10-15T11:40:00Z")

    private fun arrival(
        line: String = "6",
        order: Int = 11,
        aimed: String = "2026-10-15T13:45:30+02:00",
        expected: String = "2026-10-15T13:45:30+02:00",
        delay: Int? = 0,
        journey: String = "14442",
        vehicle: String = "",
        cancelled: Boolean = false,
        status: String = "scheduled",
    ) = ArrivalDto(
        lineRef = line,
        directionRef = "",
        journeyCode = journey,
        vehicleRef = vehicle,
        stopPointRef = "152",
        order = order,
        aimedArrivalTime = aimed,
        expectedArrivalTime = expected,
        aimedDepartureTime = aimed,
        expectedDepartureTime = expected,
        arrivalStatus = status,
        departureStatus = status,
        cancellation = cancelled,
        predictionInaccurate = false,
        delaySeconds = delay,
    )

    @Test
    fun `given a scheduled arrival when it is mapped then it becomes a departure`() {
        val departure =
            arrival().toDomain().toDeparture(headsign = null, now = now, windowMinutes = 90)!!
        assertEquals("6", departure.timetableId)
        assertEquals(Instant.parse("2026-10-15T13:45:30+02:00"), departure.departTimestamp)
        assertEquals(0, departure.delaySeconds)
        assertNull(departure.tripId, "no vehicle assigned: nothing to track")
        assertEquals("scheduled", departure.arrivalStatus)
    }

    @Test
    fun `given a known headsign when the row is mapped then the destination is the list's second line`() {
        val departure =
            arrival()
                .toDomain()
                .toDeparture(headsign = "El Cortijo", now = now, windowMinutes = 90)!!
        assertEquals("El Cortijo", departure.displayName)
    }

    @Test
    fun `given a cancelled arrival when it is mapped then it never appears as due`() {
        assertNull(
            arrival(cancelled = true)
                .toDomain()
                .toDeparture(headsign = null, now = now, windowMinutes = 90),
            "the feed flags cancellations explicitly; showing one as 'due' is the app's word against the operator's",
        )
    }

    @Test
    fun `given one bus gone and one about to arrive when they are mapped then the gone one is dropped`() {
        assertNull(
            arrival(expected = "2026-10-15T13:30:00+02:00")
                .toDomain()
                .toDeparture(headsign = null, now = now, windowMinutes = 90),
        )
        assertTrue(
            arrival(expected = "2026-10-15T13:39:30+02:00")
                .toDomain()
                .toDeparture(headsign = null, now = now, windowMinutes = 90) != null,
            "the odd departure enters the list while it is still effectively at the stop",
        )
    }

    @Test
    fun `given an arrival outside the window when it is mapped then the client drops it`() {
        val far = arrival(expected = "2026-10-15T23:50:00+02:00")
        assertNull(far.toDomain().toDeparture(headsign = null, now = now, windowMinutes = 90))
        assertNull(far.toDomain().toDeparture(headsign = null, now = now, windowMinutes = 25))
    }

    @Test
    fun `given a vehicle number from the feed when the row is mapped then it is kept and not looked up`() {
        val departure =
            arrival(vehicle = "9693", journey = "99901")
                .toDomain()
                .toDeparture(headsign = null, now = now, windowMinutes = 90)!!
        assertEquals("9693", departure.vehicleId)
        assertEquals("9693", departure.tripId)
    }

    @Test
    fun `given a delay and both clocks when the row is mapped then the feed's delay wins`() {
        val withDelay =
            arrival(delay = -60, expected = "2026-10-15T13:45:00+02:00")
                .toDomain()
                .toDeparture(headsign = null, now = now, windowMinutes = 90)!!
        assertEquals(
            -60,
            withDelay.delaySeconds,
            "negative delays are normal: the bus is ahead of schedule",
        )

        val computed =
            arrival(
                delay = null,
                aimed = "2026-10-15T13:45:30+02:00",
                expected = "2026-10-15T13:50:30+02:00",
            ).toDomain().toDeparture(headsign = null, now = now, windowMinutes = 90)!!
        assertEquals(
            300,
            computed.delaySeconds,
            "5 minutes late, worked out from aimed vs expected",
        )
    }
}
