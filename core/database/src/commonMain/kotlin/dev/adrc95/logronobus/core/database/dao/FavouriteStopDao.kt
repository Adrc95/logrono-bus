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
import dev.adrc95.logronobus.core.database.entity.FavouriteStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteStopDao {
    @Query("SELECT * FROM favourite_stops ORDER BY position")
    fun observe(): Flow<List<FavouriteStopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: FavouriteStopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<FavouriteStopEntity>)

    @Query("DELETE FROM favourite_stops WHERE stopRef = :stopRef")
    suspend fun delete(stopRef: String)

    @Query("DELETE FROM favourite_stops")
    suspend fun clear()

    @Query(
        "INSERT OR REPLACE INTO favourite_stops (stopRef, name, stopId, position) " +
            "SELECT :stopRef, :name, :stopId, COALESCE(MAX(position), -1) + 1 FROM favourite_stops",
    )
    suspend fun insertAtEnd(
        stopRef: String,
        name: String,
        stopId: String,
    )
}
