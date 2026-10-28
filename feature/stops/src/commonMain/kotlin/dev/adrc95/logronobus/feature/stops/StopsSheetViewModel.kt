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

package dev.adrc95.logronobus.feature.stops

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineLabelsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineStartsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.toRouteColors
import dev.adrc95.logronobus.core.ui.util.toRouteOrigins
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import dev.adrc95.logronobus.feature.stops.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.stops.model.StopSheetDisplayModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class StopsSheetViewModel(
    @InjectedParam private val navKey: StopSheetNavKey,
    private val getStop: GetStopUseCase,
    private val getStopDepartures: GetStopDeparturesUseCase,
    private val getStopLineStarts: GetStopLineStartsUseCase,
    private val getStopLineLabels: GetStopLineLabelsUseCase,
    private val getRoutes: GetRoutesUseCase,
    private val getFavourites: GetFavouriteStopsUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase,
    private val startTracking: StartTrackingUseCase,
) : ViewModel() {
    private var loadedStop: Stop? = null
    private var lineStarts: List<DepartureTime> = emptyList()
    private var departures: List<DepartureTime> = emptyList()
    private var lineLabels: Map<String, String> = emptyMap()
    private var routeOrigins: Map<String, String> = emptyMap()
    private var routeColors: Map<String, Color> = emptyMap()
    private var isLoading = true
    private var notLive = false
    private var isFavourite = false

    private val favouriteRefs: Flow<Set<String>> =
        getFavourites()
            .map { favourites -> favourites.map(FavouriteStop::stopRef).toSet() }

    private val sheet: Flow<StopSheetDisplayModel?> =
        flow {
            val routes = safeCall(emptyMap<String, Route>()) { getRoutes() }
            var stop =
                safeCall<Stop?>(null) {
                    getStop(navKey.stopRef)
                }
            if (stop == null) {
                delay(StopsConstants.STOP_LOOKUP_RETRY_MILLIS)
                stop = safeCall<Stop?>(null) { getStop(navKey.stopRef) }
            }
            if (stop == null) {
                isLoading = false
                emit(
                    StopSheetDisplayModel(
                        title = navKey.stopRef,
                        stopId = "",
                        stopRef = navKey.stopRef,
                        isFavourite = false,
                        rows = persistentListOf(),
                        isLoading = false,
                        notLive = false,
                        missing = true,
                    ),
                )
                return@flow
            }
            loadedStop = stop
            routeOrigins = routes.values.toList().toRouteOrigins()
            routeColors = routes.values.toList().toRouteColors()
            isLoading = true
            emit(buildDisplay())

            coroutineScope {
                val starts =
                    async {
                        safeCall(emptyList<DepartureTime>()) {
                            getStopLineStarts(
                                stop.stopRef,
                                lineFilter = navKey.lineFilter,
                                directionFilter = navKey.directionFilter,
                            )
                        }
                    }
                val labels =
                    async {
                        safeCall(emptyMap<String, String>()) { getStopLineLabels(stop.stopRef) }
                    }
                val answer =
                    async {
                        safeCall<StopDepartures?>(null) {
                            getStopDepartures(
                                stopId = stop.stopRef,
                                lineFilter = navKey.lineFilter,
                                directionFilter = navKey.directionFilter,
                            )
                        }
                    }

                lineStarts = starts.await()
                lineLabels = labels.await()
                emit(buildDisplay())

                val stopDepartures = answer.await()
                departures = stopDepartures?.times.orEmpty()
                notLive = stopDepartures?.stale ?: true
                isLoading = false
                emit(buildDisplay())
            }
        }

    val uiState: StateFlow<StopSheetDisplayModel?> =
        combine(sheet, favouriteRefs) { sheet, favourites ->
            sheet?.copy(isFavourite = sheet.stopRef in favourites)
        }.stateInViewModel(this, null)

    fun toggleFavourite() {
        val stop = loadedStop ?: return
        val alreadyFavourite = uiState.value?.isFavourite == true
        launchSafely {
            safeCall(Unit) { toggleFavourite(stop, alreadyFavourite) }
        }
    }

    fun track(departure: DepartureTime) {
        launchSafely { safeCall(Unit) { startTracking(departure, navKey.stopRef) } }
    }

    private fun buildDisplay(): StopSheetDisplayModel? =
        loadedStop?.toDisplayModel(
            lineStarts = lineStarts,
            departures = departures,
            lineLabels = lineLabels,
            routeOrigins = routeOrigins,
            routeColors = routeColors,
            isLoading = isLoading,
            notLive = notLive,
            isFavourite = isFavourite,
        )
}
