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

package dev.adrc95.logronobus.core.testing.builder

import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.model.Stop

class NearbyStopBuilder {
    var stop: Stop = stop()
    var distanceMeters: Double = 250.0
    var walkMinutes: Int? = null

    fun withStop(stop: Stop) = apply { this.stop = stop }

    fun withDistanceMeters(distanceMeters: Double) = apply { this.distanceMeters = distanceMeters }

    fun withWalkMinutes(walkMinutes: Int?) = apply { this.walkMinutes = walkMinutes }

    fun build() =
        NearbyStop(
            stop = stop,
            distanceMeters = distanceMeters,
            walkMinutes = walkMinutes,
        )
}

fun nearbyStop(block: NearbyStopBuilder.() -> Unit = {}) = NearbyStopBuilder().apply(block).build()
