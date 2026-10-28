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

package dev.adrc95.logronobus.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import dev.adrc95.logronobus.core.database.entity.CatalogueLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueLineStopEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueSyncEntity

@Dao
interface CatalogueDao {
    @Query("SELECT storedAt FROM catalogue_stop LIMIT 1")
    suspend fun stopsStoredAt(): Long?

    @Query("SELECT * FROM catalogue_stop")
    suspend fun allStops(): List<CatalogueStopEntity>

    @Query("SELECT * FROM catalogue_stop_line")
    suspend fun allStopLines(): List<CatalogueStopLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(rows: List<CatalogueStopEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopLines(rows: List<CatalogueStopLineEntity>)

    @Query("DELETE FROM catalogue_stop")
    suspend fun clearStops()

    @Query("DELETE FROM catalogue_stop_line")
    suspend fun clearStopLines()

    @Transaction
    suspend fun replaceStops(
        stops: List<CatalogueStopEntity>,
        stopLines: List<CatalogueStopLineEntity>,
        storedAt: Long,
    ) {
        clearStopLines()
        clearStops()
        if (stops.isNotEmpty()) insertStops(stops.map { it.copy(storedAt = storedAt) })
        if (stopLines.isNotEmpty()) insertStopLines(stopLines)
    }

    @Query("SELECT storedAt FROM catalogue_line LIMIT 1")
    suspend fun linesStoredAt(): Long?

    @Query("SELECT * FROM catalogue_line")
    suspend fun allLines(): List<CatalogueLineEntity>

    @Query("SELECT * FROM catalogue_line_stop")
    suspend fun allLineStops(): List<CatalogueLineStopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(rows: List<CatalogueLineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineStops(rows: List<CatalogueLineStopEntity>)

    @Query("DELETE FROM catalogue_line")
    suspend fun clearLines()

    @Query("DELETE FROM catalogue_line_stop")
    suspend fun clearLineStops()

    @Transaction
    suspend fun replaceLines(
        lines: List<CatalogueLineEntity>,
        lineStops: List<CatalogueLineStopEntity>,
        storedAt: Long,
    ) {
        clearLineStops()
        clearLines()
        if (lines.isNotEmpty()) insertLines(lines.map { it.copy(storedAt = storedAt) })
        if (lineStops.isNotEmpty()) insertLineStops(lineStops)
    }
}
