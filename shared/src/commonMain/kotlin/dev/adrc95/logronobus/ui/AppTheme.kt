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

package dev.adrc95.logronobus.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.designsystem.theme.LogronoBusTheme
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.feature.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogronoBusAppTheme(content: @Composable () -> Unit) {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    if (!settingsState.loaded) return

    val darkTheme =
        when (settingsState.themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

    ProvideAppLocale(settingsState.appLanguage) {
        LogronoBusTheme(darkTheme = darkTheme, content = content)
    }
}
