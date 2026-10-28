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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.common.time.parseInstant
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.DepartureTime

fun List<BusLocation>.keepingTheFreshest(previous: List<BusLocation>): List<BusLocation> =
    map { bus ->
        val before = previous.firstOrNull { it.tripDuid == bus.tripDuid } ?: return@map bus
        val busAt = bus.modifiedTimestamp
        val beforeAt = before.modifiedTimestamp
        if (busAt != null && beforeAt != null && busAt < beforeAt) before else bus
    }

fun DepartureTime?.keepingTheLaterHour(candidate: DepartureTime?): DepartureTime? {
    val current = this ?: return candidate
    val updated = candidate ?: return current
    if (current.vehicleId != updated.vehicleId) return updated
    val currentAt = current.departTimestamp
    val updatedAt = updated.departTimestamp
    return if (currentAt != null && updatedAt != null && updatedAt < currentAt) current else updated
}
