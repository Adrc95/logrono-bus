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

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.common.isNotNull
import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.trip.forDirection
import dev.adrc95.logronobus.core.domain.trip.isServingWithin
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteShapeUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopEtasUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopRolesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackedTripUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveLastViewedRouteUseCase
import dev.adrc95.logronobus.core.domain.usecase.StartTrackingUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.UpdateTrackedTripUseCase
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.RouteBusPositions
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopDepartures
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toEtaDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toRouteDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toRouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.mapper.toStopDisplayModels
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import dev.adrc95.logronobus.core.ui.util.NOW_TICK_MILLIS
import dev.adrc95.logronobus.core.ui.util.REFRESH_INTERVAL_MILLIS
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.titleCased
import dev.adrc95.logronobus.feature.routes.mapper.toDisplayModels
import dev.adrc95.logronobus.feature.routes.mapper.toRouteStopsDisplayModel
import dev.adrc95.logronobus.feature.routes.model.RouteDisplayModel
import dev.adrc95.logronobus.feature.routes.model.RouteStopDisplayModel
import dev.adrc95.logronobus.feature.routes.model.UpcomingStopDisplayModel
import dev.adrc95.logronobus.feature.routes.model.ViewMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
class RoutesViewModel(
    private val getRouteDetail: GetRouteDetailUseCase,
    private val getRouteShape: GetRouteShapeUseCase,
    private val getRouteBusPositions: GetRouteBusPositionsUseCase,
    private val getStopEtas: GetStopEtasUseCase,
    private val getStopDepartures: GetStopDeparturesUseCase,
    private val getLastViewedRoute: GetLastViewedRouteUseCase,
    private val saveLastViewedRoute: SaveLastViewedRouteUseCase,
    private val getRoutes: GetRoutesUseCase,
    private val getFavourites: GetFavouriteStopsUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
    private val getTrackedTrip: GetTrackedTripUseCase,
    private val updateTrackedTrip: UpdateTrackedTripUseCase,
    private val startTracking: StartTrackingUseCase,
    private val getStopRoles: GetStopRolesUseCase = GetStopRolesUseCase(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())

    private var rawRouteStops: List<List<Stop>> = emptyList()
    private var rawBusPositions: List<BusLocation> = emptyList()
    private var directionBusPositions: List<BusLocation> = emptyList()
    private var rawStopDepartures: List<DepartureTime> = emptyList()
    private var routeShapes: List<RouteLineDisplayModel> = emptyList()
    private var directionHeadsigns: List<String> = emptyList()
    private var directionLabels: List<String> = emptyList()
    private var directionOrigins: List<String> = emptyList()
    private var favouriteRefs: Set<String> = emptySet()
    private var selectedStopRef: String? = null
    private var selectedBusShape: RouteLineDisplayModel = RouteLineDisplayModel()
    private var trackedTripId: String? = null
    private var isLoadingPositions = false
    private var isRefreshing = false

    private val routes: StateFlow<List<Route>> =
        flow {
            emit(safeCall(emptyList()) { getRoutes().values.toList() })
        }.stateInViewModel(this, emptyList())

    private val favourites: StateFlow<Set<String>> =
        getFavourites()
            .map { list -> list.map { favourite -> favourite.stopRef }.toSet() }
            .onEach { refs ->
                favouriteRefs = refs
                refreshDerived()
            }.stateInViewModel(this, emptySet())

    private val tracked: StateFlow<TrackedTrip?> =
        getTrackedTrip()
            .onEach { trackedTrip ->
                trackedTripId = trackedTrip?.tripId
                _uiState.update { it.copy(trackedStopRef = trackedTrip?.stopRef) }
            }.stateInViewModel(this, null)

    private val routeLoader: StateFlow<String?> =
        _uiState
            .map { it.selectedRouteNum }
            .distinctUntilChanged()
            .onEach { routeNum -> if (routeNum.isNotNull()) loadRoute(routeNum) else clearRouteData() }
            .stateInViewModel(this, null)

    private val departuresLoader: StateFlow<Long> =
        flow {
            var loaded: Pair<String, Int>? = null
            while (true) {
                val state = _uiState.value
                val stopRef = selectedStopRef
                if (stopRef == null) {
                    loaded = null
                    clearStopDepartures()
                    delay(NOW_TICK_MILLIS)
                    continue
                }
                val key = stopRef to state.selectedDirection
                if (loaded != key) {
                    loaded = key
                    clearStopDepartures()
                    loadDepartures(
                        stopRef = stopRef,
                        lineFilter = state.selectedRouteNum,
                        directionFilter = state.selectedDirection,
                        showLoading = true,
                    )
                }
                delay(REFRESH_INTERVAL_MILLIS)
                loadDepartures(
                    stopRef = stopRef,
                    lineFilter = state.selectedRouteNum,
                    directionFilter = state.selectedDirection,
                    showLoading = false,
                )
                emit(nowEpochMilliseconds())
            }
        }.stateInViewModel(this, 0L)

    private val busShapeLoader: StateFlow<String?> =
        _uiState
            .map { it.selectedRouteBus?.tripDuid }
            .distinctUntilChanged()
            .onEach { tripDuid -> loadBusShape(tripDuid) }
            .stateInViewModel(this, null)

    private val trackedRouteLoader: StateFlow<TrackedTrip?> =
        tracked
            .drop(1)
            .onEach { trackedTrip -> trackedTrip?.departure?.let { selectRoute(it.timetableId) } }
            .stateInViewModel(this, null)

    private val lastViewedRouteLoader: StateFlow<String?> =
        flow {
            emit(safeCall<String?>(null) { getLastViewedRoute() })
        }.onEach { routeNum -> if (routeNum.isNotNull()) selectRoute(routeNum) }
            .stateInViewModel(this, null)

    private val autoRefresh: StateFlow<Long> =
        flow {
            while (true) {
                val routeNum = _uiState.value.selectedRouteNum
                if (routeNum.isNotNull()) {
                    val following = tracked.value?.tripId != null
                    delay(
                        if (following) {
                            RoutesConstants.TRACKING_REFRESH_INTERVAL_MILLIS
                        } else {
                            REFRESH_INTERVAL_MILLIS
                        },
                    )
                    refreshRoutePositions(routeNum, force = following)
                    emit(nowEpochMilliseconds())
                } else {
                    delay(NOW_TICK_MILLIS)
                }
            }
        }.stateInViewModel(this, 0L)

    private val loaders: Flow<Unit> =
        merge(
            favourites.map { },
            routeLoader.map { },
            departuresLoader.map { },
            busShapeLoader.map { },
            trackedRouteLoader.map { },
            lastViewedRouteLoader.map { },
            autoRefresh.map { },
        )

    val uiState: StateFlow<UiState> =
        combine(routes, _uiState, loaders) { routes, state, _ ->
            val display = routes.toDisplayModels()
            state.copy(
                routes = display.toImmutableList(),
                routeColor = display.firstOrNull { it.code == state.selectedRouteNum }?.lineColor,
            )
        }.stateInViewModel(this, UiState())

    fun selectRoute(routeNum: String) {
        if (_uiState.value.selectedRouteNum != routeNum) clearRouteData()
        _uiState.update {
            it.copy(
                selectedRouteNum = routeNum,
                selectedDirection = 0,
                selectedRouteBus = null,
            )
        }
        selectedStopRef = null
        launchSafely { safeCall(Unit) { saveLastViewedRoute(routeNum) } }
    }

    fun clearRoute() {
        _uiState.update {
            it.copy(
                selectedRouteNum = null,
                selectedRouteBus = null,
            )
        }
        selectedStopRef = null
        launchSafely { safeCall(Unit) { saveLastViewedRoute(null) } }
    }

    fun selectDirection(index: Int) {
        val state = _uiState.value
        if (index in rawRouteStops.indices && index != state.selectedDirection) {
            selectedBusShape = RouteLineDisplayModel()
            directionBusPositions = busesForDirection(index)
            _uiState.update {
                it.copy(
                    selectedDirection = index,
                    selectedRouteBus = null,
                )
            }
            refreshDerived()
        }
    }

    private fun busesForDirection(direction: Int): List<BusLocation> {
        val now = Instant.fromEpochMilliseconds(nowEpochMilliseconds())
        val stops = rawRouteStops.flatten()
        return rawBusPositions
            .filter { bus ->
                bus.isServingWithin(
                    minutes = RoutesConstants.MAX_NEXT_STOP_LEAD_MINUTES,
                    now = now,
                    stops = stops,
                    waitMinutes = RoutesConstants.MAX_TERMINUS_WAIT_MINUTES,
                )
            }.forDirection(
                stopRefsByDirection =
                    rawRouteStops.map { stops -> stops.mapTo(mutableSetOf()) { it.stopRef } },
                headsignsByDirection = directionHeadsigns,
                direction = direction,
            )
    }

    fun toggleDirection() {
        val count = rawRouteStops.size
        if (count > 1) {
            selectDirection((_uiState.value.selectedDirection + 1) % count)
        }
    }

    fun selectStop(stopRef: String) {
        selectedStopRef = if (selectedStopRef == stopRef) null else stopRef
        rawStopDepartures = emptyList()
        refreshDerived()
    }

    fun selectRouteBus(bus: BusLocationDisplayModel) {
        if (_uiState.value.selectedRouteBus?.tripDuid == bus.tripDuid) {
            clearRouteBusSelection()
            return
        }
        selectedBusShape = RouteLineDisplayModel()
        _uiState.update { it.copy(selectedRouteBus = bus) }
        refreshDerived()
    }

    fun clearRouteBusSelection() {
        selectedBusShape = RouteLineDisplayModel()
        _uiState.update { it.copy(selectedRouteBus = null) }
        refreshDerived()
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun refreshPositions() {
        val routeNum = _uiState.value.selectedRouteNum ?: return
        launchSafely { refreshRoutePositions(routeNum, force = true) }
    }

    fun toggleFavourite(stop: StopDisplayModel) {
        val already = stop.stopRef in favouriteRefs
        launchSafely {
            safeCall(Unit) {
                rawRouteStops.flatten().firstOrNull { it.stopRef == stop.stopRef }?.let {
                    toggleFavouriteUseCase(it, already)
                }
            }
        }
    }

    fun track(
        departure: DepartureTime,
        stopRef: String,
    ) {
        launchSafely { safeCall(Unit) { startTracking(departure, stopRef) } }
    }

    private suspend fun loadRoute(routeNum: String) {
        clearRouteData()
        isLoadingPositions = true
        val detail = safeCall<RouteDetail?>(null) { getRouteDetail(routeNum) }
        if (_uiState.value.selectedRouteNum != routeNum) {
            isLoadingPositions = false
            return
        }
        if (detail.isNotNull()) {
            rawRouteStops = detail.stops
            routeShapes = detail.shapes.toRouteDisplayModels()
            directionHeadsigns = detail.headsigns
            directionLabels = detail.labels.map { label -> label.titleCased() }
            directionOrigins = detail.origins.map { origin -> origin.titleCased() }
            val positions =
                safeCall<RouteBusPositions?>(null) {
                    getRouteBusPositions.forDirection(
                        routeNum = routeNum,
                        direction = 0,
                        stopRefsByDirection =
                            detail.stops.map { direction ->
                                direction.mapTo(mutableSetOf()) { it.stopRef }
                            },
                        headsignsByDirection = detail.headsigns,
                    )
                }
            if (positions.isNotNull()) {
                if (_uiState.value.selectedRouteNum == routeNum) {
                    rawBusPositions = positions.all
                    directionBusPositions = busesForDirection(0)
                }
            }
        } else {
            _uiState.update { it.copy(busFeedStale = true) }
        }
        isLoadingPositions = false
        refreshDerived()
    }

    private fun clearRouteData() {
        rawRouteStops = emptyList()
        rawBusPositions = emptyList()
        directionBusPositions = emptyList()
        rawStopDepartures = emptyList()
        routeShapes = emptyList()
        directionHeadsigns = emptyList()
        directionLabels = emptyList()
        directionOrigins = emptyList()
        selectedStopRef = null
        _uiState.update {
            it.copy(
                directionCount = 0,
                directions = persistentListOf(),
                routeStopRows = persistentListOf(),
                mapStops = persistentListOf(),
                mapEtas = emptyMap(),
                busPositions = persistentListOf(),
                upcomingStops = persistentListOf(),
                mapPolyline = persistentListOf(),
                busFeedStale = false,
                lastUpdatedEpochMs = null,
            )
        }
    }

    private suspend fun loadBusShape(tripDuid: String?) {
        if (tripDuid == null) {
            selectedBusShape = RouteLineDisplayModel()
            return
        }
        val shapeId = rawBusPositions.firstOrNull { it.tripDuid == tripDuid }?.shapeId ?: return
        val points = safeCall<List<GeoPoint>?>(null) { getRouteShape(shapeId) }
        if (points.isNotNull()) {
            selectedBusShape = points.toRouteLineDisplayModel()
            refreshDerived()
        }
    }

    private fun clearStopDepartures() {
        _uiState.update { it.copy(isLoadingDepartures = true) }
        if (rawStopDepartures.isEmpty()) return
        rawStopDepartures = emptyList()
        refreshDerived()
    }

    private suspend fun loadDepartures(
        stopRef: String,
        lineFilter: String?,
        directionFilter: Int?,
        showLoading: Boolean,
    ) {
        if (showLoading) _uiState.update { it.copy(isLoadingDepartures = true) }
        val answer =
            safeCall<StopDepartures?>(null) {
                getStopDepartures(
                    stopId = stopRef,
                    lineFilter = lineFilter,
                    directionFilter = directionFilter,
                )
            }
        if (selectedStopRef == stopRef) {
            if (answer.isNotNull()) {
                rawStopDepartures = answer.times
                _uiState.update { it.copy(stopDeparturesNotLive = answer.stale) }
                refreshDerived()
            } else {
                _uiState.update { it.copy(stopDeparturesNotLive = true) }
            }
            _uiState.update { it.copy(isLoadingDepartures = false) }
        }
    }

    private suspend fun refreshRoutePositions(
        routeNum: String,
        force: Boolean,
    ) {
        if (isLoadingPositions || isRefreshing) return
        isRefreshing = true
        val positions =
            safeCall<RouteBusPositions?>(null) {
                getRouteBusPositions.forDirection(
                    routeNum = routeNum,
                    direction = _uiState.value.selectedDirection,
                    stopRefsByDirection =
                        rawRouteStops.map { direction ->
                            direction.mapTo(mutableSetOf()) { it.stopRef }
                        },
                    headsignsByDirection = directionHeadsigns,
                    forceRefresh = force,
                )
            }
        if (positions.isNotNull()) {
            if (_uiState.value.selectedRouteNum != routeNum) {
                isRefreshing = false
                return
            }
            rawBusPositions = positions.all
            directionBusPositions = busesForDirection(_uiState.value.selectedDirection)
            val selected =
                _uiState.value.selectedRouteBus?.let { picked ->
                    positions.all.firstOrNull { it.tripDuid == picked.tripDuid }
                }
            _uiState.update {
                it.copy(
                    busFeedStale = positions.stale,
                    lastUpdatedEpochMs = nowEpochMilliseconds(),
                    selectedRouteBus = selected?.toDisplayModel() ?: it.selectedRouteBus,
                )
            }
            refreshDerived()
            refreshTrackedDeparture(positions.all)
        } else {
            logDebug("route=$routeNum fetch failed")
            _uiState.update { it.copy(busFeedStale = true) }
        }
        isRefreshing = false
    }

    private suspend fun refreshTrackedDeparture(fetched: List<BusLocation>) {
        val trackedTrip = tracked.value ?: return
        val row = trackedTrip.departure ?: return
        val bus =
            fetched.firstOrNull {
                (row.vehicleId != null && it.vehicleId == row.vehicleId) || it.tripDuid == row.tripId
            } ?: return
        val next = bus.nextStops?.firstOrNull { it.stopRef == trackedTrip.stopRef } ?: return
        updateTrackedTrip(
            trackedTrip.copy(
                departure =
                    row.copy(
                        delaySeconds = next.delay ?: row.delaySeconds,
                        departTimestamp = next.departureTimestamp ?: row.departTimestamp,
                    ),
            ),
        )
    }

    private fun refreshDerived() {
        val state = _uiState.value
        val stops = rawRouteStops.getOrNull(state.selectedDirection).orEmpty()
        val displayStops = stops.toStopDisplayModels()
        val raw =
            state.selectedRouteBus?.let { bus ->
                rawBusPositions.firstOrNull { it.tripDuid == bus.tripDuid }
            }
        val now = Instant.fromEpochMilliseconds(nowEpochMilliseconds())
        val selectedBus = state.selectedRouteBus
        val polyline =
            when {
                selectedBus != null && !selectedBusShape.isEmpty -> {
                    listOf(selectedBusShape)
                }

                else -> {
                    routeShapes
                        .getOrNull(state.selectedDirection)
                        ?.takeIf { shape -> !shape.isEmpty }
                        ?.let { listOf(it) }
                        .orEmpty()
                }
            }
        _uiState.update {
            it.copy(
                mapPolyline = polyline.toImmutableList(),
                mapTrackedTripId = selectedBus?.tripDuid ?: trackedTripId,
                directionCount = rawRouteStops.size,
                directions =
                    directionOrigins
                        .ifEmpty {
                            directionLabels.ifEmpty { directionHeadsigns.map { headsign -> headsign.titleCased() } }
                        }.toImmutableList(),
                routeColor = state.routeColor,
                mapStops = displayStops.toImmutableList(),
                mapEtas = getStopEtas(stops, raw, now = now).toEtaDisplayModels(),
                upcomingStops =
                    getStopEtas
                        .upcoming(
                            stops = rawRouteStops.flatten().distinctBy { stop -> stop.stopRef },
                            bus = raw,
                            now = now,
                        ).map { (stop, minutes) ->
                            UpcomingStopDisplayModel(name = stop.longName, minutes = minutes)
                        }.toImmutableList(),
                busPositions = directionBusPositions.toDisplayModels(displayStops).toImmutableList(),
                routeStopRows =
                    stops
                        .toRouteStopsDisplayModel(
                            selectedStopRef = selectedStopRef,
                            favouriteRefs = favouriteRefs,
                            departures = rawStopDepartures,
                            lineLabel = directionOrigins.getOrNull(state.selectedDirection),
                            roles = getStopRoles(stops),
                            routeColors =
                                state.selectedRouteNum
                                    ?.let { num ->
                                        state.routeColor?.let { color -> mapOf(num to color) }
                                    }.orEmpty(),
                        ).toImmutableList(),
            )
        }
    }

    data class UiState(
        val routes: ImmutableList<RouteDisplayModel> = persistentListOf(),
        val routeColor: Color? = null,
        val viewMode: ViewMode = ViewMode.MAP,
        val trackedStopRef: String? = null,
        val mapTrackedTripId: String? = null,
        val selectedRouteNum: String? = null,
        val selectedDirection: Int = 0,
        val directionCount: Int = 0,
        val directions: ImmutableList<String> = persistentListOf(),
        val routeStopRows: ImmutableList<RouteStopDisplayModel> = persistentListOf(),
        val upcomingStops: ImmutableList<UpcomingStopDisplayModel> = persistentListOf(),
        val mapPolyline: ImmutableList<RouteLineDisplayModel> = persistentListOf(),
        val mapStops: ImmutableList<StopDisplayModel> = persistentListOf(),
        val mapEtas: Map<String, StopEtaDisplayModel> = emptyMap(),
        val busPositions: ImmutableList<BusLocationDisplayModel> = persistentListOf(),
        val busFeedStale: Boolean = false,
        val selectedRouteBus: BusLocationDisplayModel? = null,
        val lastUpdatedEpochMs: Long? = null,
        val isLoadingDepartures: Boolean = true,
        val stopDeparturesNotLive: Boolean = false,
    )
}
