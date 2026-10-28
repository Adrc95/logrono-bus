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
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_DIRECTION_REF
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_HEADSIGN
import dev.adrc95.logronobus.core.database.DatabaseConstants.COLUMN_LINE_CODE
import dev.adrc95.logronobus.core.database.DatabaseConstants.TABLE_LEARNED_DIRECTIONS

@Entity(
    tableName = TABLE_LEARNED_DIRECTIONS,
    primaryKeys = [
        COLUMN_LINE_CODE,
        COLUMN_DIRECTION_REF,
    ],
)
data class LearnedDirectionEntity(
    @ColumnInfo(name = COLUMN_LINE_CODE)
    val lineCode: String,
    @ColumnInfo(name = COLUMN_DIRECTION_REF)
    val directionRef: String,
    @ColumnInfo(name = COLUMN_HEADSIGN)
    val headsign: String,
)
