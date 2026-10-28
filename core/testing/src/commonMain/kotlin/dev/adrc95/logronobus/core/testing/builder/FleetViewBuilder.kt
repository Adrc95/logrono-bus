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

import dev.adrc95.logronobus.core.model.FleetBehind
import dev.adrc95.logronobus.core.model.FleetCall
import dev.adrc95.logronobus.core.model.FleetView
import dev.adrc95.logronobus.core.model.StopSequence

class FleetViewBuilder {
    var calls: List<FleetCall> = emptyList()
    var behind: List<FleetBehind> = emptyList()
    var stopId: Int = 0
    var sequences: Map<String, List<StopSequence>> = emptyMap()

    fun withCalls(calls: List<FleetCall>) = apply { this.calls = calls }

    fun withBehind(behind: List<FleetBehind>) = apply { this.behind = behind }

    fun withStopId(stopId: Int) = apply { this.stopId = stopId }

    fun withSequences(sequences: Map<String, List<StopSequence>>) = apply { this.sequences = sequences }

    fun build() =
        FleetView(
            calls = calls,
            behind = behind,
            stopId = stopId,
            sequences = sequences,
        )
}

fun fleetView(block: FleetViewBuilder.() -> Unit = {}) = FleetViewBuilder().apply(block).build()
