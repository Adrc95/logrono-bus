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

import dev.adrc95.logronobus.core.domain.DomainConstants.TERMINUS_ARRIVAL_METRES
import dev.adrc95.logronobus.core.domain.DomainConstants.TERMINUS_OVERSHOOT_METRES
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import kotlin.math.PI
import kotlin.math.cos

fun List<Stop>.hasTripCompleted(bus: BusLocation?): Boolean {
    if (bus == null || size < 2) return false
    val terminus = last()
    val previous = this[size - 2]
    val position = GeoPoint(bus.latitude, bus.longitude)
    val end = GeoPoint(terminus.latitude, terminus.longitude)
    val away = position.distanceTo(end)
    if (away <= TERMINUS_ARRIVAL_METRES) return true
    if (away > TERMINUS_OVERSHOOT_METRES) return false
    val legLat = end.lat - previous.latitude
    val legLon = (end.lon - previous.longitude) * cos(previous.latitude * PI / 180.0)
    val relLat = position.lat - end.lat
    val relLon = (position.lon - end.lon) * cos(end.lat * PI / 180.0)
    return relLat * legLat + relLon * legLon > 0
}
