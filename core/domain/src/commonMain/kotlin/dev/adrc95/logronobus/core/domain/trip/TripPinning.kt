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

import dev.adrc95.logronobus.core.domain.DomainConstants.TRIP_MATCH_TOLERANCE_SECONDS
import dev.adrc95.logronobus.core.model.Arrival
import kotlin.math.abs
import kotlin.time.Instant

fun pinnedTrip(
    arrivals: List<Arrival>,
    followedStopRef: String,
    followedAt: Instant?,
    followedVehicleRef: String?,
    pinned: String? = null,
): String? {
    if (arrivals.isEmpty()) return pinned
    if (pinned != null && arrivals.any { it.vehicleRef == pinned }) return pinned
    followedVehicleRef?.takeIf { it.isNotBlank() }?.let { ref ->
        arrivals.firstOrNull { it.vehicleRef == ref }?.let { return it.vehicleRef }
    }
    val wanted = followedAt ?: return pinned
    return arrivals
        .filter { it.stopRef == followedStopRef }
        .mapNotNull { arrival ->
            val at = arrival.expected ?: arrival.aimed ?: return@mapNotNull null
            (arrival.vehicleRef to abs((at - wanted).inWholeSeconds))
        }.filter { (_, gap) -> gap <= TRIP_MATCH_TOLERANCE_SECONDS }
        .minByOrNull { it.second }
        ?.first
        ?: pinned
}
