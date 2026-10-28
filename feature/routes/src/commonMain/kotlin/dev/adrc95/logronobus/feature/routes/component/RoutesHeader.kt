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

package dev.adrc95.logronobus.feature.routes.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoAppBarColors
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_back_to_routes
import dev.adrc95.logronobus.core.ui.generated.resources.settings
import dev.adrc95.logronobus.core.ui.generated.resources.tab_routes
import dev.adrc95.logronobus.core.ui.generated.resources.title_route
import dev.adrc95.logronobus.feature.routes.model.ViewMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesHeader(
    modifier: Modifier = Modifier,
    compact: Boolean,
    selectedRouteNum: String?,
    viewMode: ViewMode,
    onBack: NoParamAction,
    onSelectViewMode: InputAction<ViewMode>,
    onOpenSettings: NoParamAction,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text =
                    selectedRouteNum
                        ?.let { stringResource(Res.string.title_route, it) }
                        ?: stringResource(Res.string.tab_routes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (compact && selectedRouteNum != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        LogronoIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.cd_back_to_routes),
                    )
                }
            }
        },
        actions = {
            if (!compact && selectedRouteNum != null) {
                ModeSwitcher(viewMode = viewMode, onSelect = onSelectViewMode)
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
