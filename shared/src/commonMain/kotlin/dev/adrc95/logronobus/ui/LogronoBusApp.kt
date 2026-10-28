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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.adrc95.logronobus.core.designsystem.theme.LocalTopBarHost
import dev.adrc95.logronobus.core.designsystem.theme.logronoNavigationBarContainerColor
import dev.adrc95.logronobus.core.designsystem.theme.logronoNavigationBarContentColor
import dev.adrc95.logronobus.core.designsystem.theme.logronoNavigationBarItemColors
import dev.adrc95.logronobus.core.designsystem.theme.logronoNavigationRailItemColors
import dev.adrc95.logronobus.core.navigation.Navigation
import dev.adrc95.logronobus.feature.scan.isStopScanSupported
import dev.adrc95.logronobus.feature.settings.api.navigation.SettingsNavKey
import dev.adrc95.logronobus.navigation.TopTab
import dev.adrc95.logronobus.navigation.logronoBusEntries
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    val appState = rememberLogronoBusAppState()
    val navigator = appState.navigator
    val currentTab = appState.currentTab

    LogronoBusAppTheme {
        CompositionLocalProvider(LocalTopBarHost provides appState.topBarHost) {
            val suiteItemColors =
                NavigationSuiteItemColors(
                    navigationBarItemColors = logronoNavigationBarItemColors(),
                    navigationRailItemColors = logronoNavigationRailItemColors(),
                    navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
                )
            val barContainer = logronoNavigationBarContainerColor()
            val barContent = logronoNavigationBarContentColor()
            NavigationSuiteScaffold(
                navigationSuiteColors =
                    NavigationSuiteDefaults.colors(
                        shortNavigationBarContainerColor = barContainer,
                        shortNavigationBarContentColor = barContent,
                        navigationBarContainerColor = barContainer,
                        navigationBarContentColor = barContent,
                        navigationRailContainerColor = barContainer,
                        navigationRailContentColor = barContent,
                    ),
                navigationSuiteItems = {
                    TopTab.entries
                        .filter { it != TopTab.SCAN || isStopScanSupported }
                        .forEach { tab ->
                            item(
                                selected = currentTab == tab,
                                onClick = { navigator.goToTopLevel(tab.key) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.labelRes)) },
                                colors = suiteItemColors,
                            )
                        }
                },
            ) {
                Scaffold(
                    topBar = {
                        val hostedTopBar = appState.topBarHost.content
                        when {
                            hostedTopBar != null -> {
                                hostedTopBar()
                            }

                            currentTab != null && currentTab != TopTab.ROUTES -> {
                                LogronoTopBar(
                                    currentTab = currentTab,
                                    onRefresh = navigator::refresh,
                                    onOpenSettings = { navigator.navigate(SettingsNavKey) },
                                )
                            }

                            else -> {
                                LogronoTopBarPlaceholder()
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets(0),
                    snackbarHost = { SnackbarHost(appState.snackbarHostState) },
                ) { padding ->
                    Navigation(
                        state = appState.navigationState,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .consumeWindowInsets(padding)
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                                ),
                        entryProvider = logronoBusEntries(navigator, appState::showMessage),
                    )
                }
            }
        }
    }
}
