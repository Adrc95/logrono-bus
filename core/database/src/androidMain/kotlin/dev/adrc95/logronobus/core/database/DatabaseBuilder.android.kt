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

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun databaseBuilder(context: Context): RoomDatabase.Builder<LogronoDatabase> {
    val appContext = context.applicationContext
    return Room
        .databaseBuilder<LogronoDatabase>(
            context = appContext,
            name = appContext.getDatabasePath(DatabaseConstants.FILE_NAME).absolutePath,
        ).setDriver(BundledSQLiteDriver())
}
