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

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun databaseBuilder(): RoomDatabase.Builder<LogronoDatabase> {
    val directory = dataDirectory()
    directory.mkdirs()
    return Room
        .databaseBuilder<LogronoDatabase>(File(directory, DatabaseConstants.FILE_NAME).absolutePath)
        .setDriver(BundledSQLiteDriver())
}

private fun dataDirectory(): File =
    System.getProperty("logronobus.data.dir")?.let { File(it) }
        ?: File(
            System.getProperty("user.home") ?: System.getProperty("java.io.tmpdir") ?: ".",
            ".logronobus",
        )
