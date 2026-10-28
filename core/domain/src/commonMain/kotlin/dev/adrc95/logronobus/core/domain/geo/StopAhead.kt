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

import dev.adrc95.logronobus.core.domain.DomainConstants.OFF_ROUTE_METRES
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop

fun List<Stop>.indexOfBus(bus: BusLocation?): Int = bus?.let { indexOfBus(it.latitude, it.longitude) } ?: -1

fun List<Stop>.indexOfBus(
    latitude: Double,
    longitude: Double,
): Int {
    if (isEmpty()) return -1
    val position = GeoPoint(latitude, longitude)
    val points = map { GeoPoint(it.latitude, it.longitude) }
    if (size >= 2) {
        var bestLeg = -1
        var bestDetour = Double.MAX_VALUE
        for (leg in 0 until size - 1) {
            val detour =
                position.distanceTo(
                    points[leg],
                ) +
                    position.distanceTo(
                        points[leg + 1],
                    ) -
                    points[leg].distanceTo(points[leg + 1])
            if (detour < bestDetour) {
                bestDetour = detour
                bestLeg = leg
            }
        }
        val legLength = points[bestLeg].distanceTo(points[bestLeg + 1])
        if (bestDetour <= maxOf(OFF_ROUTE_METRES, legLength / 2)) return bestLeg + 1
    }
    return indices.minByOrNull { position.distanceTo(points[it]) } ?: -1
}
