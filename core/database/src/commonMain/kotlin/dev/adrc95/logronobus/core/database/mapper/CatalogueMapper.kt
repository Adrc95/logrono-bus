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

package dev.adrc95.logronobus.core.database.mapper

import dev.adrc95.logronobus.core.database.entity.CatalogueLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopEntity
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop

fun Stop.toEntity(): CatalogueStopEntity =
    CatalogueStopEntity(
        id = stopId.toIntOrNull() ?: 0,
        name = longName,
        lat = latitude,
        lng = longitude,
        status = 0,
        storedAt = 0,
    )

fun CatalogueStopEntity.toStop(routes: List<String>): Stop =
    Stop(
        stopRef = id.toString(),
        stopId = id.toString(),
        longName = name,
        shortName = name,
        latitude = lat,
        longitude = lng,
        routes = routes,
        direction = null,
    )

fun Route.toEntity(): CatalogueLineEntity =
    CatalogueLineEntity(
        code = shortName,
        id = internalId,
        name = longName,
        colour = color,
        status = 0,
        storedAt = 0,
    )

fun CatalogueLineEntity.toRoute(): Route =
    Route(
        timetableId = code,
        longName = name,
        shortName = code,
        color = colour,
        internalId = id,
    )
