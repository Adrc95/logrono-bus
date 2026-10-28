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

package dev.adrc95.logronobus.core.domain.geo

import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop

fun BusLocation.isStandingStill(previous: List<BusLocation>): Boolean {
    val before = previous.firstOrNull { it.tripDuid == tripDuid } ?: return false
    val moved =
        GeoPoint(latitude, longitude).distanceTo(
            GeoPoint(before.latitude, before.longitude),
        )
    return moved <= DomainConstants.AT_STOP_MOVE_METRES
}

fun List<Stop>.stopAt(bus: BusLocation?): String? {
    bus ?: return null
    val point = GeoPoint(bus.latitude, bus.longitude)
    return map { stop ->
        stop.stopRef to point.distanceTo(GeoPoint(stop.latitude, stop.longitude))
    }.filter { (_, metres) -> metres <= DomainConstants.AT_STOP_METRES }
        .minByOrNull { (_, metres) -> metres }
        ?.first
}
