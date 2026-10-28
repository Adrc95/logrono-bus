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

package dev.adrc95.logronobus.feature.favourites

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.usecase.AddFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDirectionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineLabelsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopLineStartsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.RemoveFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.SearchStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.mapper.toStopDisplayModels
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.routeName
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.toRouteColors
import dev.adrc95.logronobus.core.ui.util.toRouteOrigins
import dev.adrc95.logronobus.feature.favourites.mapper.toDisplayModels
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FavouritesViewModel(
    private val getFavouritesUseCase: GetFavouriteStopsUseCase,
    private val saveFavouritesUseCase: SaveFavouriteStopsUseCase,
    private val addFavouriteUseCase: AddFavouriteUseCase,
    private val removeFavouriteUseCase: RemoveFavouriteUseCase,
    private val getStopDeparturesUseCase: GetStopDeparturesUseCase,
    private val getStopLineStartsUseCase: GetStopLineStartsUseCase,
    private val getStopLineLabelsUseCase: GetStopLineLabelsUseCase,
    private val getStopsUseCase: GetStopsUseCase,
    private val getStopDirectionsUseCase: GetStopDirectionsUseCase,
    private val getRoutesUseCase: GetRoutesUseCase,
    private val startTracking: StartTrackingUseCase,
    private val searchStops: SearchStopsUseCase,
) : ViewModel() {
    private var knownRefs: Set<String>? = null
    private var stopRefsByDirection: Map<String, List<Set<String>>> = emptyMap()
    private var stopRefsLoaded = false
    private var refreshJob: Job? = null
    private val _uiState = MutableStateFlow(UiState())
    private val times = MutableStateFlow<Map<String, List<DepartureDisplayModel>>>(emptyMap())

    private val stops: StateFlow<List<StopDisplayModel>> =
        flow {
            val loaded = loadStops()
            repairMissingStopIds(loaded)
            emit(loaded.toStopDisplayModels())
        }.stateInViewModel(this, emptyList())

    private val routes: StateFlow<List<Route>> =
        flow {
            emit(loadRoutes())
        }.stateInViewModel(this, emptyList())

    private val favourites: StateFlow<List<FavouriteStop>> =
        getFavouritesUseCase()
            .onEach { stored -> onFavouritesChanged(stored) }
            .stateInViewModel(this, emptyList())

    val uiState: StateFlow<UiState> =
        combine(
            stops,
            routes,
            favourites,
            times,
            _uiState,
        ) { stops, routes, favourites, times, state ->
            val routeNames = routes.associate { it.shortName to it.longName.routeName() }
            val routeColors = routes.toRouteColors()
            val query = state.query.trim()
            state
                .copy(
                    query = state.query,
                    isSearching = query.length >= 2,
                    lineColors = routeColors,
                    searchResults =
                        searchStops(query)
                            .toStopDisplayModels(routeNames, directionsOf())
                            .toImmutableList(),
                    cards =
                        favourites
                            .toDisplayModels(
                                stopsByRef = stops.associateBy { it.stopRef },
                                rows = times,
                            ).toImmutableList(),
                )
        }.stateInViewModel(this, UiState())

    private suspend fun directionsOf(): Map<String, List<Set<String>>> {
        if (stopRefsLoaded) return stopRefsByDirection
        stopRefsByDirection = safeCall(emptyMap<String, List<Set<String>>>()) { getStopDirectionsUseCase() }
        stopRefsLoaded = stopRefsByDirection.isNotEmpty()
        return stopRefsByDirection
    }

    private suspend fun loadStops(): List<Stop> =
        safeCall(emptyList()) {
            getStopsUseCase()
        }

    private suspend fun loadRoutes(): List<Route> =
        safeCall(emptyList()) {
            getRoutesUseCase().values.toList()
        }

    private fun onFavouritesChanged(favourites: List<FavouriteStop>) {
        val refs = favourites.mapTo(mutableSetOf()) { it.stopRef }
        val previous = knownRefs
        knownRefs = refs
        if (previous == null) {
            launchSafely {
                refreshFavouriteDeparturesInternal(stored = favourites, showLoading = true)
            }
            return
        }
        if (previous == refs) return
        val removed = previous - refs
        if (removed.isNotEmpty()) {
            times.update { all -> all - removed }
        }
        val added = favourites.filter { it.stopRef !in previous }
        if (added.isNotEmpty()) {
            launchSafely {
                if (refreshStops(added)) _uiState.update { it.copy(updatedMs = nowEpochMilliseconds()) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun isFavourite(stopRef: String): Boolean = favourites.value.any { it.stopRef == stopRef }

    fun toggleFavourite(stop: StopDisplayModel) {
        val already = isFavourite(stop.stopRef)
        val row = FavouriteStop(stopRef = stop.stopRef, name = stop.name, stopId = stop.stopId)
        launchSafely {
            safeCall(Unit) {
                if (already) removeFavouriteUseCase(stop.stopRef) else addFavouriteUseCase(row)
            }
        }
    }

    fun removeFavourite(stopRef: String) {
        launchSafely {
            safeCall(Unit) {
                removeFavouriteUseCase(stopRef)
            }
        }
    }

    fun refreshFavouriteDepartures() {
        refreshJob?.cancel()
        refreshJob =
            launchSafely(Dispatchers.Default) {
                refreshFavouriteDeparturesInternal(showLoading = true)
            }
    }

    fun track(
        departure: DepartureTime,
        stopRef: String,
    ) {
        launchSafely { safeCall(Unit) { startTracking(departure, stopRef) } }
    }

    private suspend fun refreshFavouriteDeparturesInternal(
        stored: List<FavouriteStop>? = null,
        showLoading: Boolean = false,
    ) {
        val stops = stored ?: favourites.value
        if (stops.isEmpty()) {
            times.update { emptyMap() }
            return
        }

        if (showLoading) _uiState.update { it.copy(isLoading = true) }
        try {
            val anySucceeded = refreshStops(stops)
            if (anySucceeded) _uiState.update { it.copy(updatedMs = nowEpochMilliseconds()) }
        } finally {
            if (showLoading) _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun refreshStops(stops: List<FavouriteStop>): Boolean =
        coroutineScope {
            stops
                .map { favourite ->
                    async {
                        try {
                            fetchStop(favourite)
                            true
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            times.update { all ->
                                if (favourite.stopRef in all) {
                                    all
                                } else {
                                    all + (favourite.stopRef to emptyList())
                                }
                            }
                            false
                        }
                    }
                }.awaitAll()
                .any { it }
        }

    private suspend fun fetchStop(favourite: FavouriteStop) {
        val answer = getStopDeparturesUseCase(favourite.stopRef)
        val starts = safeCall(emptyList()) { getStopLineStartsUseCase(favourite.stopRef) }
        val labels = safeCall(emptyMap()) { getStopLineLabelsUseCase(favourite.stopRef) }
        val origins = routes.value.toRouteOrigins()
        val colors = routes.value.toRouteColors()
        val rows =
            if (starts.isNotEmpty()) {
                starts.map { start ->
                    start.toDisplayModel(
                        destinationLabel = labels[start.timetableId] ?: origins[start.timetableId],
                        lineColor = colors[start.timetableId],
                    )
                }
            } else {
                answer.times.map { it.toDisplayModel() }
            }
        times.update { all -> all + (favourite.stopRef to rows) }
    }

    private suspend fun repairMissingStopIds(allStops: List<Stop>) {
        val current = getFavouritesUseCase().first()
        if (current.isEmpty() || current.none { it.stopId == "0" || it.stopId.isEmpty() }) return
        val updated =
            current.map { favourite ->
                if (favourite.stopId == "0" || favourite.stopId.isEmpty()) {
                    val stop = allStops.find { it.stopRef == favourite.stopRef }
                    if (stop != null) favourite.copy(stopId = stop.stopId) else favourite
                } else {
                    favourite
                }
            }
        if (updated != current) saveFavouritesUseCase(updated)
    }

    data class UiState(
        val cards: ImmutableList<FavouriteStopDisplayModel> = persistentListOf(),
        val query: String = "",
        val isSearching: Boolean = false,
        val searchResults: ImmutableList<StopDisplayModel> = persistentListOf(),
        val lineColors: Map<String, Color> = emptyMap(),
        val isLoading: Boolean = false,
        val updatedMs: Long? = null,
    )
}
