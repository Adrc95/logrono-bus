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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoAppBarColors
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_refresh_bus_positions
import dev.adrc95.logronobus.core.ui.generated.resources.cd_refresh_nearby
import dev.adrc95.logronobus.core.ui.generated.resources.settings
import dev.adrc95.logronobus.feature.buses.api.navigation.BusesNavKey
import dev.adrc95.logronobus.feature.nearby.api.navigation.NearbyNavKey
import dev.adrc95.logronobus.navigation.TopTab
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LogronoTopBar(
    currentTab: TopTab,
    onRefresh: InputAction<NavKey>,
    onOpenSettings: NoParamAction,
) {
    TopAppBar(
        title = { Text(stringResource(currentTab.titleRes)) },
        actions = {
            refreshActionFor(currentTab)?.let { action ->
                IconButton(onClick = { onRefresh(action.key) }) {
                    Icon(
                        LogronoIcons.Refresh,
                        contentDescription = stringResource(action.labelRes),
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    LogronoIcons.Settings,
                    contentDescription = stringResource(Res.string.settings),
                )
            }
        },
        colors = logronoAppBarColors(),
    )
}

private class RefreshAction(
    val key: NavKey,
    val labelRes: StringResource,
)

private fun refreshActionFor(tab: TopTab): RefreshAction? =
    when (tab) {
        TopTab.NEARBY -> RefreshAction(NearbyNavKey, Res.string.cd_refresh_nearby)
        TopTab.BUSES -> RefreshAction(BusesNavKey, Res.string.cd_refresh_bus_positions)
        else -> null
    }

@Composable
internal fun LogronoTopBarPlaceholder() {
    TopAppBar(
        title = {},
        colors = logronoAppBarColors(),
    )
}
