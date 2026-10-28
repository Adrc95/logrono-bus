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

import dev.adrc95.logronobus.core.model.Arrival
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ArrivalDepartureTest {
    private val now = Instant.parse("2026-10-27T17:00:00Z")

    private fun arrival(
        vehicle: String,
        at: Instant,
    ) = Arrival(
        lineRef = "3",
        directionRef = "464",
        order = 12,
        aimedArrival = at,
        expectedArrival = at + 2.minutes,
        aimedDeparture = at,
        expectedDeparture = null,
        vehicleRef = vehicle,
        stopRef = "155",
        cancellation = false,
        predictionInaccurate = false,
        delaySeconds = null,
        arrivalStatus = null,
    )

    @Test
    fun `given a departure built from the feed when it is read then the provider's own trip is kept`() {
        // When
        val departure =
            arrival(vehicle = "91", at = now + 10.minutes)
                .toDeparture(headsign = "Villamediana", now = now, windowMinutes = 90)

        // Then
        assertEquals(
            "91",
            departure?.providerTripId,
            "The line's own trip id has to survive so the follow screen can pin it exactly",
        )
        assertEquals("91", departure?.tripId)
    }
}
