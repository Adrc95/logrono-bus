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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.TripleInputAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.ui.component.BusMapView
import dev.adrc95.logronobus.core.ui.component.LastUpdatedChip
import dev.adrc95.logronobus.core.ui.component.NotLiveChip
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_refresh_positions
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun RouteMapPane(
    modifier: Modifier = Modifier,
    state: RoutesViewModel.UiState,
    nowMs: Long,
    directions: ImmutableList<String>,
    onSelectDirection: InputAction<Int>,
    onStopClick: TripleInputAction<StopDisplayModel, String?, Int?>,
    onBusClick: InputAction<BusLocationDisplayModel>,
    onDismissBus: NoParamAction,
    onRefresh: NoParamAction,
) {
    val selectedRouteNum = state.selectedRouteNum ?: return
    Box(modifier) {
        if (state.mapStops.isEmpty() && state.mapPolyline.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        } else {
            key(selectedRouteNum) {
                BusMapView(
                    positions = state.busPositions,
                    stops = state.mapStops,
                    trackedTripId = state.mapTrackedTripId,
                    trackedStopRef = state.trackedStopRef,
                    onStopClick = { onStopClick(it, state.selectedRouteNum, state.selectedDirection) },
                    onBusClick = onBusClick,
                    polylines = state.mapPolyline,
                    stopEtas = state.mapEtas,
                    routeColor = state.routeColor,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DirectionSwitcher(
                directionCount = state.directionCount,
                selectedDirection = state.selectedDirection,
                directionHeadsigns = directions,
                onSelectDirection = onSelectDirection,
                floating = true,
            )
            if (state.busFeedStale) NotLiveChip()
            state.selectedRouteBus?.let { bus ->
                SelectedBusCard(
                    bus = bus,
                    upcoming = state.upcomingStops,
                    lineColor = bus.lineColor,
                    onDismiss = onDismissBus,
                )
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.lastUpdatedEpochMs?.let { updatedMs ->
                LastUpdatedChip(
                    updatedMs = updatedMs,
                    nowMs = nowMs,
                    stale = state.busFeedStale,
                )
            }
            SmallFloatingActionButton(onClick = onRefresh) {
                Icon(
                    LogronoIcons.Refresh,
                    contentDescription = stringResource(Res.string.cd_refresh_positions),
                )
            }
        }
    }
}
