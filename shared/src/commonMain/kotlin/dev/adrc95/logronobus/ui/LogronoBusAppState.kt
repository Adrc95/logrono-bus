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

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.adrc95.logronobus.core.designsystem.theme.TopBarHostState
import dev.adrc95.logronobus.core.navigation.NavigationState
import dev.adrc95.logronobus.core.navigation.Navigator
import dev.adrc95.logronobus.core.navigation.rememberNavigationState
import dev.adrc95.logronobus.feature.favourites.api.navigation.FavouritesNavKey
import dev.adrc95.logronobus.navigation.TopTab
import dev.adrc95.logronobus.navigation.appNavConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class LogronoBusAppState(
    val navigationState: NavigationState,
    val navigator: Navigator,
    val snackbarHostState: SnackbarHostState,
    val snackbarScope: CoroutineScope,
    val topBarHost: TopBarHostState,
) {
    val currentTab: TopTab? by derivedStateOf {
        TopTab.entries.firstOrNull { it.key == navigationState.currentKey }
    }

    fun showMessage(message: String) {
        snackbarScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }
}

@Composable
fun rememberLogronoBusAppState(): LogronoBusAppState {
    val navigationState = rememberNavigationState(appNavConfiguration, FavouritesNavKey)
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val topBarHost = remember { TopBarHostState() }
    return remember(
        navigationState,
        navigator,
        snackbarHostState,
        snackbarScope,
        topBarHost,
    ) {
        LogronoBusAppState(
            navigationState = navigationState,
            navigator = navigator,
            snackbarHostState = snackbarHostState,
            snackbarScope = snackbarScope,
            topBarHost = topBarHost,
        )
    }
}
