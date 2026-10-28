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

package dev.adrc95.logronobus.feature.routes.mapper

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopRole
import dev.adrc95.logronobus.core.ui.mapper.toDirectionRows
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.util.routeCodeComparator
import dev.adrc95.logronobus.core.ui.util.toLineColor
import dev.adrc95.logronobus.core.ui.util.withoutCodePrefix
import dev.adrc95.logronobus.feature.routes.model.RouteDisplayModel
import dev.adrc95.logronobus.feature.routes.model.RouteStopDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun Route.toDisplayModel(): RouteDisplayModel =
    RouteDisplayModel(
        code = shortName,
        name = longName.withoutCodePrefix(),
        lineColor = color.toLineColor(),
        lineLabelColor = color.toLineColor()?.readableOnColor(),
    )

fun List<Route>.toDisplayModels(): List<RouteDisplayModel> =
    sortedWith(compareBy(routeCodeComparator()) { it.shortName }).map { it.toDisplayModel() }

fun List<Stop>.toRouteStopsDisplayModel(
    selectedStopRef: String?,
    favouriteRefs: Set<String>,
    departures: List<DepartureTime>,
    lineLabel: String?,
    roles: List<StopRole>,
    routeColors: Map<String, Color> = emptyMap(),
): List<RouteStopDisplayModel> =
    mapIndexed { index, stop ->
        val expanded = stop.stopRef == selectedStopRef
        val role = roles.getOrElse(index) { StopRole.MIDDLE }
        RouteStopDisplayModel(
            stop = stop.toDisplayModel(),
            isFavourite = stop.stopRef in favouriteRefs,
            isExpanded = expanded,
            departures =
                if (expanded) {
                    departures
                        .toDirectionRows(
                            stop = stop,
                            lineLabel = lineLabel,
                            isDirectionOrigin = role == StopRole.ORIGIN,
                            isDirectionEnd = role == StopRole.END,
                            routeColors = routeColors,
                        ).toImmutableList()
                } else {
                    persistentListOf()
                },
        )
    }
