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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.platform.localeChangeRequiresRestart
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.ProvideTopBar
import dev.adrc95.logronobus.core.designsystem.theme.logronoAppBarColors
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.model.ThemeMode
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_back
import dev.adrc95.logronobus.core.ui.generated.resources.language
import dev.adrc95.logronobus.core.ui.generated.resources.language_english
import dev.adrc95.logronobus.core.ui.generated.resources.language_spanish
import dev.adrc95.logronobus.core.ui.generated.resources.language_system
import dev.adrc95.logronobus.core.ui.generated.resources.settings
import dev.adrc95.logronobus.core.ui.generated.resources.theme
import dev.adrc95.logronobus.core.ui.generated.resources.theme_dark
import dev.adrc95.logronobus.core.ui.generated.resources.theme_light
import dev.adrc95.logronobus.core.ui.generated.resources.theme_system
import dev.adrc95.logronobus.core.ui.generated.resources.version
import dev.adrc95.logronobus.feature.settings.component.FlagEmoji
import dev.adrc95.logronobus.feature.settings.component.RestartForLanguageDialog
import dev.adrc95.logronobus.feature.settings.component.SettingsRadioRow
import dev.adrc95.logronobus.feature.settings.component.SettingsSection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    settings: SettingsViewModel = koinViewModel(),
    onBack: NoParamAction,
) {
    val settingsState by settings.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        settingsState = settingsState,
        onSelectThemeMode = settings::selectThemeMode,
        onSelectAppLanguage = settings::selectAppLanguage,
        onBack = onBack,
    )
}

@Composable
fun SettingsScreen(
    settingsState: SettingsViewModel.UiState,
    onSelectThemeMode: InputAction<ThemeMode>,
    onSelectAppLanguage: InputAction<String?>,
    onBack: NoParamAction,
) {
    var showRestart by remember { mutableStateOf(false) }
    if (showRestart) {
        RestartForLanguageDialog(onDismiss = { showRestart = false })
    }
    ProvideTopBar {
        TopAppBar(
            title = { Text(stringResource(Res.string.settings)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        LogronoIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back),
                    )
                }
            },
            colors = logronoAppBarColors(),
        )
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
    ) {
        SettingsSection(stringResource(Res.string.theme)) {
            SettingsCard {
                SettingsRadioRow(
                    label = stringResource(Res.string.theme_system),
                    selected = settingsState.themeMode == ThemeMode.SYSTEM,
                    onSelect = { onSelectThemeMode(ThemeMode.SYSTEM) },
                ) { LeadingIcon(LogronoIcons.ThemeSystem) }
                SettingsDivider()
                SettingsRadioRow(
                    label = stringResource(Res.string.theme_light),
                    selected = settingsState.themeMode == ThemeMode.LIGHT,
                    onSelect = { onSelectThemeMode(ThemeMode.LIGHT) },
                ) { LeadingIcon(LogronoIcons.ThemeLight) }
                SettingsDivider()
                SettingsRadioRow(
                    label = stringResource(Res.string.theme_dark),
                    selected = settingsState.themeMode == ThemeMode.DARK,
                    onSelect = { onSelectThemeMode(ThemeMode.DARK) },
                ) { LeadingIcon(LogronoIcons.ThemeDark) }
            }
        }
        SettingsSection(stringResource(Res.string.language)) {
            SettingsCard {
                val currentLang = settingsState.currentLanguage
                val onPickLanguage: InputAction<String?> = { tag ->
                    onSelectAppLanguage(tag)
                    if (localeChangeRequiresRestart) showRestart = true
                }
                SettingsRadioRow(
                    label = stringResource(Res.string.language_system),
                    selected = currentLang == null,
                    onSelect = { onPickLanguage(null) },
                ) { LeadingIcon(LogronoIcons.Language) }
                SettingsDivider()
                SettingsRadioRow(
                    label = stringResource(Res.string.language_english),
                    selected = currentLang == "en",
                    onSelect = { onPickLanguage("en") },
                ) { FlagEmoji("\uD83C\uDDEC\uD83C\uDDE7") }
                SettingsDivider()
                SettingsRadioRow(
                    label = stringResource(Res.string.language_spanish),
                    selected = currentLang == "es",
                    onSelect = { onPickLanguage("es") },
                ) { FlagEmoji("\uD83C\uDDEA\uD83C\uDDF8") }
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.version,
                    settingsState.appVersion.substringBefore(" ("),
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        colors = logronoCardColors(),
        shape = RoundedCornerShape(20.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.selectableGroup()) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun LeadingIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxSize(),
    )
}
