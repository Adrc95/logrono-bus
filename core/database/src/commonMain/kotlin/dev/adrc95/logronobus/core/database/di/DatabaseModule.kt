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

package dev.adrc95.logronobus.core.database.di

import androidx.room3.RoomDatabase
import dev.adrc95.logronobus.core.database.LogronoDatabase
import dev.adrc95.logronobus.core.database.dao.CatalogueDao
import dev.adrc95.logronobus.core.database.dao.FaresDao
import dev.adrc95.logronobus.core.database.dao.FavouriteStopDao
import dev.adrc95.logronobus.core.database.dao.LearnedDirectionDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("dev.adrc95.logronobus.core.database")
class DatabaseModule {
    @Single
    fun provideDatabase(builder: RoomDatabase.Builder<LogronoDatabase>): LogronoDatabase = builder.build()

    @Single
    fun provideFavouriteStopDao(database: LogronoDatabase): FavouriteStopDao = database.favouriteStops()

    @Single
    fun provideLearnedDirectionDao(database: LogronoDatabase): LearnedDirectionDao = database.learnedDirections()

    @Single
    fun provideCatalogueDao(database: LogronoDatabase): CatalogueDao = database.catalogue()

    @Single
    fun provideFaresDao(database: LogronoDatabase): FaresDao = database.fares()
}
