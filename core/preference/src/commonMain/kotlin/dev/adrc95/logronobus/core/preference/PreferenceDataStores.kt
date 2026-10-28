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

package dev.adrc95.logronobus.core.preference

import androidx.datastore.core.DataStore
import androidx.datastore.core.Storage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dev.adrc95.logronobus.core.common.OutputAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

internal fun createDataStore(producePath: OutputAction<String>): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = preferenceScope(),
        produceFile = { producePath().toPath() },
    )

internal fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        storage = storage,
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = preferenceScope(),
    )

private fun preferenceScope(): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

private val dataStores = mutableMapOf<String, DataStore<Preferences>>()

internal fun createDataStoreOnce(path: String): DataStore<Preferences> =
    dataStores.getOrPut(path) { createDataStore { path } }
