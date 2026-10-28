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

package dev.adrc95.logronobus.feature.favourites.mapper

import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import kotlinx.collections.immutable.toImmutableList

fun List<FavouriteStop>.toDisplayModels(
    stopsByRef: Map<String, StopDisplayModel>,
    rows: Map<String, List<DepartureDisplayModel>>,
): List<FavouriteStopDisplayModel> =
    map { favourite ->
        val stop = stopsByRef[favourite.stopRef]
        val departures = rows[favourite.stopRef].orEmpty()
        FavouriteStopDisplayModel(
            stopRef = favourite.stopRef,
            name = stop?.name ?: favourite.name,
            stopId = stop?.stopId?.takeIf { it.isNotEmpty() && it != "0" } ?: favourite.stopId,
            rows = departures.toImmutableList(),
            hasLoadedDepartures = favourite.stopRef in rows,
        )
    }
