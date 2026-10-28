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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adrc95.logronobus.core.domain.usecase.GetAppLanguageUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPlatformInfoUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetThemeModeUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveAppLanguageUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveThemeModeUseCase
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    getAppLanguage: GetAppLanguageUseCase,
    getThemeMode: GetThemeModeUseCase,
    private val saveAppLanguage: SaveAppLanguageUseCase,
    private val saveThemeMode: SaveThemeModeUseCase,
    getPlatformInfo: GetPlatformInfoUseCase,
) : ViewModel() {
    private val appVersion = getPlatformInfo().appVersion

    val uiState: StateFlow<UiState> =
        combine(
            getAppLanguage(),
            getThemeMode(),
        ) { language, themeMode ->
            UiState(
                appLanguage = language,
                currentLanguage = language?.substringBefore('-'),
                themeMode = themeMode,
                appVersion = appVersion,
                loaded = true,
            )
        }.stateInViewModel(this, UiState(appVersion = appVersion))

    fun selectAppLanguage(tag: String?) {
        viewModelScope.launch { saveAppLanguage(tag) }
    }

    fun selectThemeMode(mode: ThemeMode) {
        viewModelScope.launch { saveThemeMode(mode) }
    }

    data class UiState(
        val appLanguage: String? = null,
        val currentLanguage: String? = null,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val appVersion: String = "",
        val loaded: Boolean = false,
    )
}
