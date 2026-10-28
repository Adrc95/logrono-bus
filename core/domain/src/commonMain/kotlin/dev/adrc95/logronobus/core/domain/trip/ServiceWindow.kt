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

import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import kotlin.time.Instant

fun BusLocation.isServingWithin(
    minutes: Long,
    now: Instant,
    stops: List<Stop>,
    waitMinutes: Long = minutes,
): Boolean {
    val next = nextStops.orEmpty().firstOrNull() ?: return true
    val at = next.departureTimestamp ?: return true
    val ahead = (at - now).inWholeMinutes
    if (ahead <= minutes) return true
    if (ahead > waitMinutes) return false
    val stop = stops.firstOrNull { it.stopRef == next.stopRef } ?: return true
    val metres =
        GeoPoint(latitude, longitude)
            .distanceTo(GeoPoint(stop.latitude, stop.longitude))
    return metres <= DomainConstants.AT_STOP_METRES
}
