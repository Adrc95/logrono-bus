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

package dev.adrc95.logronobus.core.domain.geo

import dev.adrc95.logronobus.core.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeoPointBearingTest {
    private val lat = 42.461701
    private val lon = -2.40653

    @Test
    fun `given a bearing when it is read then it points the way it was given`() {
        assertEquals(0f, GeoPoint(lat, lon).bearingTo(GeoPoint(lat + 0.01, lon))!!, 0.01f, "north")
        assertEquals(90f, GeoPoint(lat, lon).bearingTo(GeoPoint(lat, lon + 0.01))!!, 0.01f, "east")
        assertEquals(
            180f,
            GeoPoint(lat, lon).bearingTo(GeoPoint(lat - 0.01, lon))!!,
            0.01f,
            "south",
        )
        assertEquals(270f, GeoPoint(lat, lon).bearingTo(GeoPoint(lat, lon - 0.01))!!, 0.01f, "west")
    }

    @Test
    fun `given equal steps of latitude and longitude when the bearing is read then it is not north-east`() {
        val bearing = GeoPoint(lat, lon).bearingTo(GeoPoint(lat + 0.01, lon + 0.01))!!
        assertTrue(bearing in 30f..44f, "measured in metres, not in degrees: $bearing°")
    }

    @Test
    fun `given a bus that has not moved when its bearing is read then there is none`() {
        assertNull(
            GeoPoint(lat, lon).bearingTo(GeoPoint(lat, lon)),
            "no movement, no heading to invent",
        )
        assertNull(GeoPoint(lat, lon).bearingTo(GeoPoint(lat + 1e-12, lon - 1e-12)))
    }
}
