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

package dev.adrc95.logronobus.feature.stops.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.stops.model.StopSheetDisplayModel
import kotlinx.collections.immutable.toImmutableList

fun Stop?.toDisplayModel(
    lineStarts: List<DepartureTime>,
    departures: List<DepartureTime>,
    lineLabels: Map<String, String>,
    routeOrigins: Map<String, String>,
    routeColors: Map<String, Color>,
    isLoading: Boolean,
    notLive: Boolean,
    isFavourite: Boolean,
): StopSheetDisplayModel? {
    val stop = this ?: return null
    val rows =
        lineStarts
            .map { start ->
                start.toDisplayModel(
                    destinationLabel =
                        lineLabels[start.timetableId]
                            ?: routeOrigins[start.timetableId],
                    lineColor = routeColors[start.timetableId],
                )
            }.ifEmpty { departures.map { it.toDisplayModel() } }
    return StopSheetDisplayModel(
        title = stop.longName,
        stopId = stop.stopId,
        stopRef = stop.stopRef,
        isFavourite = isFavourite,
        rows = rows.toImmutableList(),
        isLoading = isLoading && lineStarts.isEmpty(),
        notLive = notLive,
    )
}
