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

package dev.adrc95.logronobus.core.preference.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.preference.PreferenceConstants.LANGUAGE
import dev.adrc95.logronobus.core.preference.PreferenceConstants.LAST_VIEWED_ROUTE
import dev.adrc95.logronobus.core.preference.PreferenceConstants.THEME
import dev.adrc95.logronobus.core.preference.get
import dev.adrc95.logronobus.core.preference.observe
import dev.adrc95.logronobus.core.preference.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [PreferencesDataSource::class])
class DataStorePreferencesDataSource(
    private val settings: DataStore<Preferences>,
) : PreferencesDataSource {
    override suspend fun lastViewedRoute(): String? = settings.get(LAST_VIEWED_ROUTE, "").ifBlank { null }

    override suspend fun saveLastViewedRoute(routeNum: String?) = settings.set(LAST_VIEWED_ROUTE, routeNum)

    override fun language(): Flow<String?> = settings.observe(LANGUAGE, "").map { tag -> tag.ifBlank { null } }

    override suspend fun saveLanguage(tag: String?) = settings.set(LANGUAGE, tag)

    override fun themeMode(): Flow<ThemeMode> =
        settings.observe(THEME, "").map { stored ->
            ThemeMode.entries.firstOrNull { mode -> mode.name == stored } ?: ThemeMode.SYSTEM
        }

    override suspend fun saveThemeMode(mode: ThemeMode) = settings.set(THEME, mode.name)
}
