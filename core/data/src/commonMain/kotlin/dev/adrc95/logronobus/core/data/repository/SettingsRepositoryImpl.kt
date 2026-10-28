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

package dev.adrc95.logronobus.core.data.repository

import dev.adrc95.logronobus.core.domain.repository.SettingsRepository
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.preference.datasource.PreferencesDataSource
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory(binds = [SettingsRepository::class])
class SettingsRepositoryImpl(
    private val preferences: PreferencesDataSource,
) : SettingsRepository {
    override val language: Flow<String?> = preferences.language()

    override val themeMode: Flow<ThemeMode> = preferences.themeMode()

    override suspend fun saveLanguage(tag: String?) = preferences.saveLanguage(tag)

    override suspend fun saveThemeMode(mode: ThemeMode) = preferences.saveThemeMode(mode)
}
