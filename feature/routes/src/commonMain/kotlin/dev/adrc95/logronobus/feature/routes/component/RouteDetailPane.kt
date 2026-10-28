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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.TripleInputAction
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import dev.adrc95.logronobus.feature.routes.model.ViewMode

@Composable
fun RouteDetailPane(
    modifier: Modifier = Modifier,
    state: RoutesViewModel.UiState,
    nowMs: Long,
    onSelectDirection: InputAction<Int>,
    onSelectStop: InputAction<String>,
    onToggleFavourite: InputAction<StopDisplayModel>,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    onStopClick: TripleInputAction<StopDisplayModel, String?, Int?>,
    onBusClick: InputAction<BusLocationDisplayModel>,
    onDismissBus: NoParamAction,
    onRefresh: NoParamAction,
) {
    if (state.selectedRouteNum == null) {
        NoRoutePlaceholder(modifier)
        return
    }
    when (state.viewMode) {
        ViewMode.STOPS -> {
            RouteStopsPanel(
                modifier = modifier,
                stops = state.routeStopRows,
                directionCount = state.directionCount,
                selectedDirection = state.selectedDirection,
                directions = state.directions,
                onSelectDirection = onSelectDirection,
                isLoadingDepartures = state.isLoadingDepartures,
                departuresNotLive = state.stopDeparturesNotLive,
                predictedStopRefs = state.mapEtas.keys,
                onStopClick = onSelectStop,
                onToggleFavourite = onToggleFavourite,
                nowMs = nowMs,
                onDepartureClick = onDepartureClick,
            )
        }

        ViewMode.MAP -> {
            RouteMapPane(
                modifier = modifier,
                state = state,
                nowMs = nowMs,
                directions = state.directions,
                onSelectDirection = onSelectDirection,
                onStopClick = onStopClick,
                onBusClick = onBusClick,
                onDismissBus = onDismissBus,
                onRefresh = onRefresh,
            )
        }
    }
}
