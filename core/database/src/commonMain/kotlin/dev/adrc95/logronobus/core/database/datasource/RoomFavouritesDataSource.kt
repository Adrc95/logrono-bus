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

import dev.adrc95.logronobus.core.database.dao.FavouriteStopDao
import dev.adrc95.logronobus.core.database.dao.LearnedDirectionDao
import dev.adrc95.logronobus.core.database.entity.LearnedDirectionEntity
import dev.adrc95.logronobus.core.database.mapper.toDomain
import dev.adrc95.logronobus.core.database.mapper.toEntity
import dev.adrc95.logronobus.core.model.FavouriteStop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [FavouritesDataSource::class])
class RoomFavouritesDataSource(
    private val favouriteStops: FavouriteStopDao,
    private val learnedDirections: LearnedDirectionDao,
) : FavouritesDataSource {
    override fun observeFavouriteStops(): Flow<List<FavouriteStop>> =
        favouriteStops.observe().map { rows -> rows.map { it.toDomain() } }

    override suspend fun addFavourite(stop: FavouriteStop) {
        favouriteStops.insertAtEnd(stop.stopRef, stop.name, stop.stopId)
    }

    override suspend fun removeFavourite(stopRef: String) {
        favouriteStops.delete(stopRef)
    }

    override suspend fun replaceFavouriteStops(favourites: List<FavouriteStop>) {
        favouriteStops.clear()
        favouriteStops.upsertAll(
            favourites.mapIndexed { index, favourite ->
                favourite.toEntity(
                    index,
                )
            },
        )
    }

    override suspend fun getLearnedDirections(): Map<String, Map<String, String>> =
        learnedDirections
            .all()
            .groupBy({ it.lineCode }, { it.directionRef to it.headsign })
            .mapValues { (_, directions) -> directions.toMap() }

    override suspend fun saveLearnedDirections(learned: Map<String, Map<String, String>>) {
        val rows =
            learned.flatMap { (lineCode, directions) ->
                directions.map { (directionRef, headsign) ->
                    LearnedDirectionEntity(
                        lineCode = lineCode,
                        directionRef = directionRef,
                        headsign = headsign,
                    )
                }
            }
        learnedDirections.clear()
        learnedDirections.insertAll(rows)
    }
}
