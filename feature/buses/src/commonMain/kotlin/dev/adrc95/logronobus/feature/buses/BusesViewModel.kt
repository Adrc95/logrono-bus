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

import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.fleet.isMeaningfullyStale
import dev.adrc95.logronobus.core.domain.usecase.GetBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.model.FleetState
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.loading_live_buses
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toStopDisplayModels
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.REFRESH_INTERVAL_MILLIS
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.feature.buses.mapper.fleetDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetDisplayModel
import dev.adrc95.logronobus.feature.buses.model.FleetStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class BusesViewModel(
    private val getBusPositions: GetBusPositionsUseCase,
    private val getStops: GetStopsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    private val stops: StateFlow<List<StopDisplayModel>> =
        flow {
            emit(safeCall(emptyList()) { getStops() }.toStopDisplayModels())
        }.stateInViewModel(this, emptyList())

    private val feed: StateFlow<FleetState> =
        flow {
            while (true) {
                emit(getBusPositions())
                delay(REFRESH_INTERVAL_MILLIS)
            }
        }.stateInViewModel(this, FleetState.Loading)

    val uiState: StateFlow<UiState> =
        combine(feed, stops) { feed, stops ->
            val positions = (feed as? FleetState.Ready)?.positions
            val buses = positions?.all?.toDisplayModels(stops).orEmpty()
            UiState(
                fleet =
                    fleetDisplayModel(
                        buses = buses,
                        hasLoaded = feed !is FleetState.Loading,
                        feedStale = feed is FleetState.Failed || positions?.isMeaningfullyStale == true,
                        updatedMs = if (buses.isNotEmpty()) nowEpochMilliseconds() else null,
                    ),
                stops = stops.toImmutableList(),
            )
        }.stateInViewModel(this, UiState())

    data class UiState(
        val fleet: FleetDisplayModel =
            FleetDisplayModel(
                status = FleetStatus.Loading(Res.string.loading_live_buses),
                updatedMs = null,
            ),
        val stops: ImmutableList<StopDisplayModel> = persistentListOf(),
    )
}
