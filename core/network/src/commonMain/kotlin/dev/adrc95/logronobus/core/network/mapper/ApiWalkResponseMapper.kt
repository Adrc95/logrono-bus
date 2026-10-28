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

package dev.adrc95.logronobus.core.network.mapper

import dev.adrc95.logronobus.core.model.WalkDestination
import dev.adrc95.logronobus.core.model.WalkDistance
import dev.adrc95.logronobus.core.network.dto.ApiWalkResponse
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

fun ApiWalkResponse.toDomain(destinations: List<WalkDestination>): List<WalkDistance> {
    val byId =
        items
            .mapNotNull { item ->
                val id =
                    item.id?.let { element ->
                        if (element is JsonPrimitive) element.content else element.toString()
                    } ?: return@mapNotNull null
                val distance = item.distanceM ?: return@mapNotNull null
                val duration = ((item.durationSec ?: 0) / 60.0).roundToInt()
                id to WalkDistance(id, distance, duration)
            }.toMap()
    return destinations.mapNotNull { byId[it.id] }
}
