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

package dev.adrc95.logronobus.feature.buses

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.ui.component.BusMapView
import dev.adrc95.logronobus.core.ui.component.NotLiveChip
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.rememberNowMs
import dev.adrc95.logronobus.feature.buses.component.EmptyFleetNotice
import dev.adrc95.logronobus.feature.buses.component.FaresButton
import dev.adrc95.logronobus.feature.buses.component.LastUpdatedCard
import dev.adrc95.logronobus.feature.buses.model.FleetDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AllBusesRoute(
    modifier: Modifier = Modifier,
    viewModel: BusesViewModel = koinViewModel(),
    onStopClick: InputAction<StopDisplayModel>,
    onOpenFares: NoParamAction,
) {
    val busState by viewModel.uiState.collectAsStateWithLifecycle()
    AllBusesScreen(
        modifier = modifier,
        busState = busState,
        nowMs = rememberNowMs(),
        onStopClick = onStopClick,
        onOpenFares = onOpenFares,
    )
}

@Composable
fun AllBusesScreen(
    modifier: Modifier = Modifier,
    busState: BusesViewModel.UiState,
    nowMs: Long,
    onStopClick: InputAction<StopDisplayModel>,
    onOpenFares: NoParamAction,
) {
    val fleet = busState.fleet
    Box(modifier) {
        BusMapView(
            positions = fleet.buses,
            stops = busState.stops,
            onStopClick = { onStopClick(it) },
            onBusClick = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (fleet.status is FleetStatus.Unavailable) {
            NotLiveChip(Modifier.align(Alignment.TopStart).padding(12.dp))
        } else {
            fleet.updatedMs?.let { updatedMs ->
                LastUpdatedCard(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    updatedMs = updatedMs,
                    nowMs = nowMs,
                )
            }
        }
        FaresButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            onClick = onOpenFares,
        )
        EmptyFleetNotice(
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            fleet = fleet,
        )
    }
}
