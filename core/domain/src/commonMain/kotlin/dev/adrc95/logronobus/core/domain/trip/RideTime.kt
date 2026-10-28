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

import dev.adrc95.logronobus.core.domain.DomainConstants.METRES_PER_MINUTE
import dev.adrc95.logronobus.core.domain.DomainConstants.STREET_DETOUR
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import kotlin.math.ceil

fun List<Stop>.minutesFromStartTo(stopRef: String): Int? {
    val index = indexOfFirst { it.stopRef == stopRef }
    if (index < 0) return null
    if (index == 0) return 0
    var metres = 0.0
    for (i in 1..index) {
        val from = GeoPoint(this[i - 1].latitude, this[i - 1].longitude)
        val to = GeoPoint(this[i].latitude, this[i].longitude)
        metres += from.distanceTo(to)
    }
    return ceil(metres * STREET_DETOUR / METRES_PER_MINUTE).toInt()
}
