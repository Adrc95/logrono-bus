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

package dev.adrc95.logronobus.core.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_NAME
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_POSITION
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STOP_ID
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STOP_REF
import dev.adrc95.logronobus.core.database.DatabaseConstants.TABLE_FAVOURITE_STOPS

@Entity(
    tableName = TABLE_FAVOURITE_STOPS,
)
data class FavouriteStopEntity(
    @PrimaryKey @ColumnInfo(name = COLUMN_STOP_REF)
    val stopRef: String,
    @ColumnInfo(name = COLUMN_NAME)
    val name: String,
    @ColumnInfo(name = COLUMN_STOP_ID)
    val stopId: String,
    @ColumnInfo(name = COLUMN_POSITION)
    val position: Int,
)
