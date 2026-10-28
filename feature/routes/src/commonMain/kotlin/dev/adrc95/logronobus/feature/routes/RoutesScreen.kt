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

package dev.adrc95.logronobus.feature.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.TripleInputAction
import dev.adrc95.logronobus.core.designsystem.isCompactWidth
import dev.adrc95.logronobus.core.designsystem.theme.ProvideTopBar
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardContainerColor
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.rememberNowMs
import dev.adrc95.logronobus.feature.routes.component.ModeSwitcher
import dev.adrc95.logronobus.feature.routes.component.RouteDetailPane
import dev.adrc95.logronobus.feature.routes.component.RouteListPanel
import dev.adrc95.logronobus.feature.routes.component.RoutesHeader
import dev.adrc95.logronobus.feature.routes.model.RouteDisplayModel
import dev.adrc95.logronobus.feature.routes.model.ViewMode

@Composable
fun RoutesScreen(
    viewModel: RoutesViewModel,
    onStopClick: TripleInputAction<StopDisplayModel, String?, Int?>,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    onOpenSettings: NoParamAction,
) {
    val compact = isCompactWidth()
    val nowMs = rememberNowMs()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = compact && state.selectedRouteNum != null,
        onBackCompleted = { viewModel.clearRoute() },
    )

    ProvideTopBar {
        Column(
            modifier = Modifier.background(logronoCardContainerColor()),
        ) {
            RoutesHeader(
                compact = compact,
                selectedRouteNum = state.selectedRouteNum,
                viewMode = state.viewMode,
                onBack = viewModel::clearRoute,
                onSelectViewMode = { viewModel.setViewMode(it) },
                onOpenSettings = onOpenSettings,
            )
            if (compact && state.selectedRouteNum != null) {
                ModeSwitcher(
                    viewMode = state.viewMode,
                    onSelect = { viewModel.setViewMode(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            HorizontalDivider()
        }
    }

    val onRouteSelected: InputAction<RouteDisplayModel> = {
        viewModel.selectRoute(it.code)
        viewModel.setViewMode(ViewMode.MAP)
    }

    val detailPane: @Composable (Modifier) -> Unit = { paneModifier ->
        RouteDetailPane(
            modifier = paneModifier,
            state = state,
            nowMs = nowMs,
            onSelectDirection = { viewModel.selectDirection(it) },
            onSelectStop = { viewModel.selectStop(it) },
            onToggleFavourite = { viewModel.toggleFavourite(it) },
            onDepartureClick = { departure, stopRef -> onDepartureClick(departure, stopRef) },
            onStopClick = onStopClick,
            onBusClick = { viewModel.selectRouteBus(it) },
            onDismissBus = { viewModel.clearRouteBusSelection() },
            onRefresh = { viewModel.refreshPositions() },
        )
    }

    if (compact) {
        if (state.selectedRouteNum == null) {
            RouteListPanel(
                routes = state.routes,
                selectedRouteNum = null,
                onRouteSelected = onRouteSelected,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            detailPane(Modifier.fillMaxSize())
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            RouteListPanel(
                routes = state.routes,
                selectedRouteNum = state.selectedRouteNum,
                onRouteSelected = onRouteSelected,
                modifier = Modifier.width(200.dp).fillMaxHeight(),
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            detailPane(Modifier.weight(1f).fillMaxHeight())
        }
    }
}
