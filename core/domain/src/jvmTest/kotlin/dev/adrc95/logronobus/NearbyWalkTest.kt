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

import dev.adrc95.logronobus.core.domain.geo.withWalkingDistances
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.WalkDistance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NearbyWalkTest {
    private fun stop(
        ref: String,
        name: String,
        lat: Double,
        lng: Double,
    ) = Stop(
        stopRef = ref,
        stopId = ref,
        longName = name,
        shortName = name,
        latitude = lat,
        longitude = lng,
    )

    private val origin = GeoPoint(42.4653, -2.4448)

    @Test
    fun `given a walking answer when the distances are shown then it replaces the straight line`() {
        val nearby =
            listOf(
                NearbyStop(stop("1", "Cerca", 42.4654, -2.4449), distanceMeters = 20.0),
                NearbyStop(stop("2", "Lejos", 42.4700, -2.4500), distanceMeters = 600.0),
            )
        val walked = listOf(WalkDistance(id = "1", distanceM = 85, durationMin = 1))

        val merged = nearby.withWalkingDistances(walked)

        assertEquals(85.0, merged[0].distanceMeters, 1e-9, "walked, not as the crow flies")
        assertEquals(1, merged[0].walkMinutes)
        assertEquals(
            600.0,
            merged[1].distanceMeters,
            1e-9,
            "no answer for this one: keep the honest fallback",
        )
        assertNull(merged[1].walkMinutes)
    }

    @Test
    fun `given walking times when the list is built then it stays ordered by the distance shown`() {
        val nearby =
            listOf(
                NearbyStop(stop("1", "A", 42.4654, -2.4449), distanceMeters = 20.0),
                NearbyStop(stop("2", "B", 42.4656, -2.4451), distanceMeters = 40.0),
            )

        val walked =
            listOf(
                WalkDistance(id = "1", distanceM = 320, durationMin = 4),
                WalkDistance(id = "2", distanceM = 60, durationMin = 1),
            )

        val merged = nearby.withWalkingDistances(walked)

        assertEquals(
            listOf("2", "1"),
            merged.map { it.stop.stopRef },
            "the list re-sorts on the real number",
        )
    }

    @Test
    fun `given no walking answer when the list is built then nothing is invented`() {
        val nearby = listOf(NearbyStop(stop("1", "A", 42.4654, -2.4449), distanceMeters = 20.0))
        val merged = nearby.withWalkingDistances(emptyList())
        assertEquals(20.0, merged.single().distanceMeters, 1e-9)
        assertNull(merged.single().walkMinutes)
        assertTrue(merged.single().distanceMeters > 0)
    }
}
