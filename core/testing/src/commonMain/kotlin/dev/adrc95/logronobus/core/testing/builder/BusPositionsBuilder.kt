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

import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.BusPositions

class BusPositionsBuilder {
    var byRoute: Map<String, List<BusLocation>> = emptyMap()
    var stale: Boolean = false
    var staleSeconds: Long? = null

    fun withByRoute(byRoute: Map<String, List<BusLocation>>) = apply { this.byRoute = byRoute }

    fun withStale(stale: Boolean) = apply { this.stale = stale }

    fun withStaleSeconds(staleSeconds: Long?) = apply { this.staleSeconds = staleSeconds }

    fun build() =
        BusPositions(
            byRoute = byRoute,
            stale = stale,
            staleSeconds = staleSeconds,
        )
}

fun busPositions(block: BusPositionsBuilder.() -> Unit = {}) = BusPositionsBuilder().apply(block).build()
