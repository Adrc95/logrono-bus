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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal inline fun <reified T> DataStore<Preferences>.observe(
    key: String,
    default: T,
): Flow<T> = data.map { preferences -> preferences[preferenceKey<T>(key)] ?: default }

internal suspend inline fun <reified T> DataStore<Preferences>.get(
    key: String,
    default: T,
): T = data.first()[preferenceKey<T>(key)] ?: default

internal suspend inline fun <reified T> DataStore<Preferences>.set(
    key: String,
    value: T?,
) {
    val prefKey = preferenceKey<T>(key)
    edit { preferences ->
        if (value == null) preferences.remove(prefKey) else preferences[prefKey] = value
    }
}

internal suspend inline fun <reified T> DataStore<Preferences>.remove(key: String) {
    val prefKey = preferenceKey<T>(key)
    edit { preferences -> preferences.remove(prefKey) }
}
