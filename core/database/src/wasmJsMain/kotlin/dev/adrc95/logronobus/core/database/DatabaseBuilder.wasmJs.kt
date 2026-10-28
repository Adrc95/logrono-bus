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
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

fun databaseBuilder(): RoomDatabase.Builder<LogronoDatabase> =
    Room
        .databaseBuilder<LogronoDatabase>(name = DatabaseConstants.FILE_NAME)
        .setDriver(webDriver())

internal fun webDriver(): SQLiteDriver = WebWorkerSQLiteDriver(sqliteWorker())

/**
 * The web keeps the database in the browser's Origin-Private File System, and the worker is what
 * does the work off the main thread. The script is vendored from the AndroidX repo, which does not
 * publish it: see `core/database/src/wasmJsMain/web-worker/`. The npm package that resolves the URL is
 * declared by the app, because npm dependencies of a library are not propagated to webpack.
 */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun sqliteWorker(): Worker =
    js("""new Worker(new URL("@logronobus/sqlite-web-worker/worker.js", import.meta.url))""")
