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

package dev.adrc95.logronobus.feature.tracking.model

import dev.adrc95.logronobus.feature.tracking.model.FollowedBus
import dev.adrc95.logronobus.feature.tracking.model.preferredVehicleId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TrackingHandoverTest {
    @Test
    fun `given a bus remembered for this ride when the model is built then it is the one kept`() {
        assertEquals(
            "1702",
            preferredVehicleId(FollowedBus(rideGeneration = 3, vehicleId = "1702"), 3),
        )
    }

    @Test
    fun `given a bus remembered for the previous ride when this ride is read then it does not answer for it`() {
        assertNull(preferredVehicleId(FollowedBus(rideGeneration = 3, vehicleId = "1702"), 4))
    }

    @Test
    fun `given nothing remembered when the model is built then it is free to choose`() {
        assertNull(preferredVehicleId(null, 1))
    }
}
