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
import androidx.room3.ForeignKey
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_ID
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_LINE_ID
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STOP_ID
import dev.adrc95.logronobus.core.database.DatabaseConstants.TABLE_CATALOGUE_STOP_LINE

@Entity(
    tableName = TABLE_CATALOGUE_STOP_LINE,
    primaryKeys = [
        COLUMN_STOP_ID,
        COLUMN_LINE_ID,
    ],
    foreignKeys = [
        ForeignKey(
            entity = CatalogueStopEntity::class,
            parentColumns = [COLUMN_ID],
            childColumns = [COLUMN_STOP_ID],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CatalogueStopLineEntity(
    @ColumnInfo(name = COLUMN_STOP_ID)
    val stopId: Int,
    @ColumnInfo(name = COLUMN_LINE_ID)
    val lineId: Int,
)
