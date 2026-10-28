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
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_ID
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_LAT
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_LNG
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_NAME
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STATUS
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STORED_AT
import dev.adrc95.logronobus.core.database.DatabaseConstants.TABLE_CATALOGUE_STOP

@Entity(
    tableName = TABLE_CATALOGUE_STOP,
)
data class CatalogueStopEntity(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID)
    val id: Int,
    @ColumnInfo(name = COLUMN_NAME)
    val name: String,
    @ColumnInfo(name = COLUMN_LAT)
    val lat: Double,
    @ColumnInfo(name = COLUMN_LNG)
    val lng: Double,
    @ColumnInfo(name = COLUMN_STATUS)
    val status: Int,
    @ColumnInfo(name = COLUMN_STORED_AT)
    val storedAt: Long = 0,
)
