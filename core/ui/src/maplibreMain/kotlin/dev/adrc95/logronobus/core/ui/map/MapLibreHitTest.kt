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

package dev.adrc95.logronobus.core.ui.map

import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Position

internal fun List<StopDisplayModel>.nearestTo(tapped: Position?): StopDisplayModel? {
    if (size <= 1 || tapped == null) return firstOrNull()
    return minByOrNull { stop ->
        metres(
            tapped.latitude,
            tapped.longitude,
            stop.location.latitude,
            stop.location.longitude,
        )
    }
}

internal fun List<BusLocationDisplayModel>.nearestBusTo(tapped: Position?): BusLocationDisplayModel? {
    if (size <= 1 || tapped == null) return firstOrNull()
    return minByOrNull { bus ->
        metres(
            tapped.latitude,
            tapped.longitude,
            bus.location.latitude,
            bus.location.longitude,
        )
    }
}

internal fun List<Feature<Geometry, JsonObject?>>.toStops(all: List<StopDisplayModel>): List<StopDisplayModel> =
    mapNotNull { feature ->
        val ref =
            feature.properties
                ?.get("ref")
                ?.jsonPrimitive
                ?.contentOrNull
        all.firstOrNull { it.stopRef == ref }
    }

internal fun List<Feature<Geometry, JsonObject?>>.toBuses(
    all: List<BusLocationDisplayModel>,
): List<BusLocationDisplayModel> =
    mapNotNull { feature ->
        val ref =
            feature.properties
                ?.get("ref")
                ?.jsonPrimitive
                ?.contentOrNull
        all.firstOrNull { (it.vehicleId ?: it.tripDuid) == ref }
    }
