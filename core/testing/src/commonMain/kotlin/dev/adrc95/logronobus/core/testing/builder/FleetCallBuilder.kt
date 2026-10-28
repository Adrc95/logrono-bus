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
import dev.adrc95.logronobus.core.model.FleetCall
import kotlin.time.Instant

class FleetCallBuilder {
    var vehicleId: String = "sample"
    var lineCode: String = "sample"
    var direction: Int = 0
    var at: Instant = Instant.parse("2026-10-18T12:00:00Z")
    var delaySeconds: Int? = null
    var row: DepartureTime = departure()

    fun withVehicleId(vehicleId: String) = apply { this.vehicleId = vehicleId }

    fun withLineCode(lineCode: String) = apply { this.lineCode = lineCode }

    fun withDirection(direction: Int) = apply { this.direction = direction }

    fun withAt(at: Instant) = apply { this.at = at }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun withRow(row: DepartureTime) = apply { this.row = row }

    fun build() =
        FleetCall(
            vehicleId = vehicleId,
            lineCode = lineCode,
            direction = direction,
            at = at,
            delaySeconds = delaySeconds,
            row = row,
        )
}

fun fleetCall(block: FleetCallBuilder.() -> Unit = {}) = FleetCallBuilder().apply(block).build()
