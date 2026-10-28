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

import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopDepartures

class StopDeparturesBuilder {
    var times: List<DepartureTime> = emptyList()
    var stale: Boolean = false
    var staleSeconds: Long? = null

    fun withTimes(times: List<DepartureTime>) = apply { this.times = times }

    fun withStale(stale: Boolean) = apply { this.stale = stale }

    fun withStaleSeconds(staleSeconds: Long?) = apply { this.staleSeconds = staleSeconds }

    fun build() =
        StopDepartures(
            times = times,
            stale = stale,
            staleSeconds = staleSeconds,
        )
}

fun stopDepartures(block: StopDeparturesBuilder.() -> Unit = {}) = StopDeparturesBuilder().apply(block).build()
