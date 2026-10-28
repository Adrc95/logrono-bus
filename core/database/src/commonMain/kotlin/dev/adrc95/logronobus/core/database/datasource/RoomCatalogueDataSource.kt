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

package dev.adrc95.logronobus.core.database.datasource

import dev.adrc95.logronobus.core.database.DatabaseConstants
import dev.adrc95.logronobus.core.database.dao.CatalogueDao
import dev.adrc95.logronobus.core.database.entity.CatalogueStopLineEntity
import dev.adrc95.logronobus.core.database.mapper.toEntity
import dev.adrc95.logronobus.core.database.mapper.toRoute
import dev.adrc95.logronobus.core.database.mapper.toStop
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import org.koin.core.annotation.Single

@Single(binds = [CatalogueLocalDataSource::class])
class RoomCatalogueDataSource(
    private val dao: CatalogueDao,
) : CatalogueLocalDataSource {
    override suspend fun getStops(): List<Stop> {
        val rows = dao.allStops()
        if (rows.isEmpty()) return emptyList()
        val codesById = dao.allLines().associate { it.id to it.code }
        val linesByStop =
            dao
                .allStopLines()
                .groupBy { it.stopId }
                .mapValues { entry ->
                    entry.value.map { row -> codesById[row.lineId] ?: row.lineId.toString() }
                }
        return rows.map { row -> row.toStop(linesByStop.getOrElse(row.id) { emptyList() }) }
    }

    override suspend fun saveStops(stops: List<Stop>) {
        val idsByCode = dao.allLines().associate { it.code to it.id }
        dao.replaceStops(
            stops = stops.map { it.toEntity() },
            stopLines =
                stops.flatMap { stop ->
                    (stop.routes.orEmpty()).mapNotNull { code ->
                        val lineId = idsByCode[code] ?: code.toIntOrNull() ?: return@mapNotNull null
                        CatalogueStopLineEntity(
                            stopId = stop.stopId.toIntOrNull() ?: 0,
                            lineId = lineId,
                        )
                    }
                },
            storedAt =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )
    }

    override suspend fun getLines(): List<Route> {
        val rows = dao.allLines()
        if (rows.isEmpty()) return emptyList()
        return rows.map { it.toRoute() }
    }

    override suspend fun saveLines(routes: List<Route>) {
        dao.replaceLines(
            lines = routes.map { it.toEntity() },
            lineStops = emptyList(),
            storedAt =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )
    }

    override suspend fun isExpired(): Boolean {
        val storedAt = dao.stopsStoredAt() ?: return true
        return kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds() - storedAt > DatabaseConstants.CATALOGUE_TTL_MILLIS
    }
}
