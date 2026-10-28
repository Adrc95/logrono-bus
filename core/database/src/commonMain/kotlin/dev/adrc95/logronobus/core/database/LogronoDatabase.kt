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

package dev.adrc95.logronobus.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import dev.adrc95.logronobus.core.database.dao.CatalogueDao
import dev.adrc95.logronobus.core.database.dao.FaresDao
import dev.adrc95.logronobus.core.database.dao.FavouriteStopDao
import dev.adrc95.logronobus.core.database.dao.LearnedDirectionDao
import dev.adrc95.logronobus.core.database.entity.CatalogueLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueLineStopEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueSyncEntity
import dev.adrc95.logronobus.core.database.entity.FareEntity
import dev.adrc95.logronobus.core.database.entity.FavouriteStopEntity
import dev.adrc95.logronobus.core.database.entity.LearnedDirectionEntity

@Database(
    entities = [
        FavouriteStopEntity::class,
        LearnedDirectionEntity::class,
        CatalogueStopEntity::class,
        CatalogueStopLineEntity::class,
        FareEntity::class,
        CatalogueLineEntity::class,
        CatalogueLineStopEntity::class,
        CatalogueSyncEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(LogronoDatabaseConstructor::class)
abstract class LogronoDatabase : RoomDatabase() {
    abstract fun favouriteStops(): FavouriteStopDao

    abstract fun learnedDirections(): LearnedDirectionDao

    abstract fun catalogue(): CatalogueDao

    abstract fun fares(): FaresDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object LogronoDatabaseConstructor : RoomDatabaseConstructor<LogronoDatabase> {
    override fun initialize(): LogronoDatabase
}
