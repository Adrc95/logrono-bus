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

import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.preference.PreferenceConstants.THEME
import dev.adrc95.logronobus.core.preference.set
import dev.adrc95.logronobus.core.testing.fake.FakePreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStorePreferencesDataSourceTest {
    private val store = FakePreferencesStore()
    private val preferences = DataStorePreferencesDataSource(store)

    @Test
    fun `given nothing stored when the last route is read then it is null`() =
        runTest {
            assertNull(preferences.lastViewedRoute())
        }

    @Test
    fun `given a saved last route when it is read then the saved route comes back`() =
        runTest {
            preferences.saveLastViewedRoute("10")
            assertEquals("10", preferences.lastViewedRoute())
        }

    @Test
    fun `given a saved last route when it is cleared then it reads as null`() =
        runTest {
            preferences.saveLastViewedRoute("10")
            preferences.saveLastViewedRoute(null)
            assertNull(preferences.lastViewedRoute())
        }

    @Test
    fun `given nothing stored when the language is observed then it emits null`() =
        runTest {
            assertNull(preferences.language().first())
        }

    @Test
    fun `given a saved language when it is observed then it emits the tag`() =
        runTest {
            preferences.saveLanguage("eu")
            assertEquals("eu", preferences.language().first())
        }

    @Test
    fun `given a blank language when it is observed then it emits null`() =
        runTest {
            preferences.saveLanguage("")
            assertNull(preferences.language().first())
        }

    @Test
    fun `given nothing stored when the theme is observed then it emits the system mode`() =
        runTest {
            assertEquals(ThemeMode.SYSTEM, preferences.themeMode().first())
        }

    @Test
    fun `given a saved theme when it is observed then it emits that mode`() =
        runTest {
            preferences.saveThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, preferences.themeMode().first())
        }

    @Test
    fun `given a stored theme that is not known when it is observed then it emits the system mode`() =
        runTest {
            store.set(THEME, "sepia")
            assertEquals(ThemeMode.SYSTEM, preferences.themeMode().first())
        }
}
