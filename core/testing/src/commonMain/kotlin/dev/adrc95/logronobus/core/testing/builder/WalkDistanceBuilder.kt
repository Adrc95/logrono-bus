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

import dev.adrc95.logronobus.core.model.WalkDistance

class WalkDistanceBuilder {
    var id: String = "360"
    var distanceM: Int = 250
    var durationMin: Int = 3

    fun withId(id: String) = apply { this.id = id }

    fun withDistanceM(distanceM: Int) = apply { this.distanceM = distanceM }

    fun withDurationMin(durationMin: Int) = apply { this.durationMin = durationMin }

    fun build() = WalkDistance(id = id, distanceM = distanceM, durationMin = durationMin)
}

fun walkDistance(block: WalkDistanceBuilder.() -> Unit = {}) = WalkDistanceBuilder().apply(block).build()
