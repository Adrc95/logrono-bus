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

package dev.adrc95.logronobus.core.ui.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.common.InputOutputAction
import dev.adrc95.logronobus.core.domain.departure.terminatesHere
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.util.titleCased

fun DepartureTime.toDisplayModel(
    destinationLabel: String? = null,
    stopName: String? = null,
    isDirectionOrigin: Boolean = false,
    isDirectionEnd: Boolean = false,
    lineColor: Color? = null,
): DepartureDisplayModel =
    DepartureDisplayModel(
        lineCode = timetableId,
        destination =
            when {
                terminatesHere(this, stopName) -> {
                    DepartureDestination.TerminatesHere
                }

                isDirectionOrigin || startsHere -> {
                    DepartureDestination.StartsHere
                }

                isDirectionEnd -> {
                    DepartureDestination.TerminatesHere
                }

                else -> {
                    DepartureDestination.Towards(
                        (destinationLabel?.takeIf { it.isNotBlank() } ?: displayName).titleCased(),
                    )
                }
            },
        departure = this,
        isTrackable = tripId != null,
        vehicleId = vehicleId,
        lineColor = lineColor,
    )

fun List<DepartureTime>.toDisplayModels(
    stopName: String? = null,
    destinationLabelFor: InputOutputAction<DepartureTime, String?> = { null },
    routeColors: Map<String, Color> = emptyMap(),
): List<DepartureDisplayModel> =
    map { departure ->
        departure.toDisplayModel(
            destinationLabel = destinationLabelFor(departure),
            stopName = stopName,
            lineColor = routeColors[departure.timetableId],
        )
    }

fun List<DepartureTime>.toDirectionRows(
    stop: Stop,
    lineLabel: String?,
    isDirectionOrigin: Boolean,
    isDirectionEnd: Boolean,
    routeColors: Map<String, Color> = emptyMap(),
): List<DepartureDisplayModel> =
    map { departure ->
        departure.toDisplayModel(
            destinationLabel = lineLabel,
            stopName = stop.longName,
            isDirectionOrigin = isDirectionOrigin,
            isDirectionEnd = isDirectionEnd,
            lineColor = routeColors[departure.timetableId],
        )
    }
