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

package dev.adrc95.logronobus.feature.settings

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.usecase.GetAppLanguageUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPlatformInfoUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetThemeModeUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveAppLanguageUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveThemeModeUseCase
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.fake.FakePlatformRepository
import dev.adrc95.logronobus.core.testing.fake.FakeSettingsRepository
import dev.adrc95.logronobus.feature.settings.SettingsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val settings = FakeSettingsRepository()
    private val platform = FakePlatformRepository()

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() =
        SettingsViewModel(
            getAppLanguage = GetAppLanguageUseCase(settings),
            getThemeMode = GetThemeModeUseCase(settings),
            saveAppLanguage = SaveAppLanguageUseCase(settings),
            saveThemeMode = SaveThemeModeUseCase(settings),
            getPlatformInfo = GetPlatformInfoUseCase(platform),
        )

    @Test
    fun `given a saved language and theme when the screen opens then both are read`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            settings.language.value = "es-ES"
            settings.themeMode.value = ThemeMode.DARK

            // When
            // Then
            createViewModel().uiState.test {
                val state = awaitItem()
                assertTrue(state.loaded)
                assertEquals("es-ES", state.appLanguage)
                assertEquals("es", state.currentLanguage)
                assertEquals(ThemeMode.DARK, state.themeMode)
                assertEquals("1.0.0", state.appVersion)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the settings screen when a theme is chosen then it is saved`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val viewModel = createViewModel()

            viewModel.selectThemeMode(ThemeMode.LIGHT)

            // Then
            assertEquals(ThemeMode.LIGHT, settings.themeMode.value)
        }
}
