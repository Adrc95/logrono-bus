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

import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.WalkDistance

internal fun List<Stop>.nearestTo(
    origin: GeoPoint,
    limit: Int = 20,
): List<NearbyStop> =
    map { stop ->
        NearbyStop(
            stop,
            origin.distanceTo(
                GeoPoint(stop.latitude, stop.longitude),
            ),
        )
    }.sortedBy { it.distanceMeters }
        .take(limit)

internal fun List<NearbyStop>.withWalkingDistances(walked: List<WalkDistance>): List<NearbyStop> {
    if (walked.isEmpty()) return this
    val byId = walked.associateBy { it.id }
    return map { nearby ->
        val walk = byId[nearby.stop.stopRef]
        if (walk == null) {
            nearby
        } else {
            nearby.copy(distanceMeters = walk.distanceM.toDouble(), walkMinutes = walk.durationMin)
        }
    }.sortedBy { it.distanceMeters }
}
