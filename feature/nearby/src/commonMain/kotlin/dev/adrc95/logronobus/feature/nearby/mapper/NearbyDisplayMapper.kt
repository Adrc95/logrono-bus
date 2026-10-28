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

package dev.adrc95.logronobus.feature.nearby.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.util.routeCodeComparator
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyStopDisplayModel
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

fun List<NearbyStop>.toDisplayModel(
    favouriteRefs: Set<String>,
    isFallback: Boolean,
    routeColors: Map<String, Color> = emptyMap(),
    routeOrigins: Map<String, String> = emptyMap(),
): NearbyDisplayModel =
    NearbyDisplayModel(
        stops =
            map { candidate ->
                val lineCodes =
                    candidate.stop.routes
                        .orEmpty()
                        .distinct()
                        .sortedWith(routeCodeComparator())
                NearbyStopDisplayModel(
                    stop = candidate.stop.toDisplayModel(),
                    distance = candidate.distanceMeters.toDistanceDisplayModel(),
                    walkMinutes = candidate.walkMinutes,
                    lineCodes = lineCodes.toImmutableList(),
                    lineColors = lineCodes.mapNotNull { code -> routeColors[code]?.let { code to it } }.toMap(),
                    lineOrigins = lineCodes.mapNotNull { code -> routeOrigins[code]?.let { code to it } }.toMap(),
                    isFavourite = candidate.stop.stopRef in favouriteRefs,
                )
            }.toImmutableList(),
        isFallback = isFallback,
    )

private fun Double.toDistanceDisplayModel(): DistanceDisplayModel =
    if (this < 1000) {
        DistanceDisplayModel.Metres(roundToInt())
    } else {
        DistanceDisplayModel.Kilometres((this / 100).roundToInt() / 10.0)
    }
