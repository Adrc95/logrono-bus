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

import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.ui.map.followTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CameraFollowTest {
    private val camera = GeoPoint(42.4620, -2.4450)

    private fun northOf(metres: Double) = GeoPoint(42.4620 + metres / 111_320.0, -2.4450)

    @Test
    fun `given a bus inside the deadband when the camera follows then it is left where it is`() {
        assertNull(followTarget(bus = northOf(10.0), camera = camera))
    }

    @Test
    fun `given a bus past the deadband when the camera follows then it is brought back to the middle`() {
        val bus = northOf(60.0)
        assertEquals(bus, followTarget(bus = bus, camera = camera))
    }

    @Test
    fun `given a gap the old wide deadband ignored when the camera follows then it already moves`() {
        val bus = northOf(40.0)
        assertEquals(
            bus,
            followTarget(bus = bus, camera = camera),
            "40 m is a bus and a half away: follow it",
        )
    }

    @Test
    fun `given no bus when the camera follows then there is nowhere to go`() {
        assertNull(followTarget(bus = null, camera = camera))
    }

    @Test
    fun `given a bus and no camera yet when it follows then it is centred`() {
        val bus = northOf(5.0)
        assertEquals(bus, followTarget(bus = bus, camera = null))
    }
}
