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
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_SOURCE
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_STORED_AT
import dev.adrc95.logronobus.core.database.DatabaseConstants.TABLE_CATALOGUE_SYNC

@Entity(
    tableName = TABLE_CATALOGUE_SYNC,
)
data class CatalogueSyncEntity(
    @PrimaryKey @ColumnInfo(name = COLUMN_SOURCE)
    val source: String,
    @ColumnInfo(name = COLUMN_STORED_AT)
    val storedAt: Long,
)
