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

package dev.adrc95.logronobus.feature.tracking

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.common.platform.logDebug
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.usecase.GetNextBusAtStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetPinnedArrivalsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteShapeUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackedTripUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetTrackingViewUseCase
import dev.adrc95.logronobus.core.domain.usecase.UpdateTrackedTripUseCase
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.PinnedArrivals
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.TrackedTrip
import dev.adrc95.logronobus.core.model.TrackingViewInput
import dev.adrc95.logronobus.core.ui.mapper.toRouteDisplayModels
import dev.adrc95.logronobus.core.ui.mapper.toRouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.util.REFRESH_INTERVAL_MILLIS
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.toRouteColors
import dev.adrc95.logronobus.feature.tracking.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.FollowedBus
import dev.adrc95.logronobus.feature.tracking.model.TrackingDisplayModel
import dev.adrc95.logronobus.feature.tracking.model.preferredVehicleId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
class TrackingViewModel(
    private val getRouteShape: GetRouteShapeUseCase,
    private val getRouteBusPositions: GetRouteBusPositionsUseCase,
    private val getRouteDetail: GetRouteDetailUseCase,
    private val getRoutes: GetRoutesUseCase,
    private val getNextBusAtStop: GetNextBusAtStopUseCase,
    private val getTrackedTrip: GetTrackedTripUseCase,
    private val getPinnedArrivals: GetPinnedArrivalsUseCase,
    private val updateTrackedTrip: UpdateTrackedTripUseCase,
    private val getTrackingView: GetTrackingViewUseCase = GetTrackingViewUseCase(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    private var routeStops: List<List<Stop>> = emptyList()
    private var directionHeadsigns: List<String> = emptyList()
    private var routeShapes: List<RouteLineDisplayModel> = emptyList()
    private var trackedShape: RouteLineDisplayModel = RouteLineDisplayModel()
    private var tripId: String? = null
    private var stopRef: String? = null
    private var departure: DepartureTime? = null
    private var lineArrivals: Map<String, Instant> = emptyMap()
    private var pinnedTripRef: String? = null
    private var nextBus: DepartureTime? = null
    private var nextBusKnown = false
    private var startedAtMs = 0L
    private var linePositions: List<BusLocation> = emptyList()
    private var previousPositions: List<BusLocation> = emptyList()
    private var rideGeneration = 0
    private var completed = false
    private var followedBus: FollowedBus? = null
    private var lastLoggedSignature: String? = null
    private var loadedTrackedShapeId: String? = null
    private var trackedShapeJob: Job? = null
    private var arrivalsJob: Job? = null
    private var nextBusJob: Job? = null
    private var positionsJob: Job? = null
    private var routeJob: Job? = null

    private val routes: StateFlow<List<Route>> =
        flow {
            emit(safeCall(emptyList()) { getRoutes().values.toList() })
        }.stateInViewModel(this, emptyList())

    private val tracked: StateFlow<TrackedTrip?> =
        getTrackedTrip()
            .stateInViewModel(this, null)

    private val followed: StateFlow<TrackedTrip?> =
        tracked
            .onEach { trackedTrip -> onTrackedChanged(trackedTrip) }
            .stateInViewModel(this, null)

    val uiState: StateFlow<UiState> =
        combine(
            routes,
            _uiState,
            followed,
        ) { routes, state, _ ->
            state.copy(tracking = buildDisplay(routes.toRouteColors()))
        }.stateInViewModel(this, UiState())

    private fun onTrackedChanged(trackedTrip: TrackedTrip?) {
        val departure = trackedTrip?.departure ?: return
        val stopRef = trackedTrip.stopRef ?: return
        if (trackedTrip.tripId == tripId && stopRef == this.stopRef) {
            if (departure != this.departure) {
                this.departure = departure
                refreshDisplay()
            }
            return
        }
        applyFollowedTrip(departure, stopRef)
    }

    fun followNextBus() {
        val next = nextBus ?: return
        val stopRef = stopRef ?: return
        setTrackedDeparture(next, stopRef)
    }

    fun updateDeparture(row: DepartureTime) {
        departure = row
        refreshDisplay()
        launchSafely { publish() }
    }

    fun setTrackedDeparture(
        departure: DepartureTime,
        stopRef: String,
    ) {
        applyFollowedTrip(departure, stopRef)
        launchSafely { publish() }
    }

    private suspend fun publish() {
        updateTrackedTrip(TrackedTrip(tripId = tripId, stopRef = stopRef, departure = departure))
    }

    private fun applyFollowedTrip(
        departure: DepartureTime,
        stopRef: String,
    ) {
        tripId = departure.tripId
        this.stopRef = stopRef
        this.departure = departure
        lineArrivals = emptyMap()
        pinnedTripRef = null
        nextBus = null
        nextBusKnown = false
        linePositions = emptyList()
        previousPositions = emptyList()
        completed = false
        rideGeneration += 1
        refreshDisplay()
        startArrivalsPolling(
            code = departure.timetableId,
            followedStopRef = stopRef,
            followedAt = departure.departTimestamp,
            followedVehicleRef =
                departure.providerTripId ?: departure.vehicleId ?: departure.tripId,
        )
        startNextBusPolling(
            code = departure.timetableId,
            followedStopRef = stopRef,
            followedAt = departure.departTimestamp,
            followedVehicleId = departure.vehicleId,
            followedTripId = departure.tripId,
        )
        startPositionsPolling(code = departure.timetableId)
        loadRoute(code = departure.timetableId)
    }

    private fun loadRoute(code: String) {
        routeJob?.cancel()
        routeJob =
            launchSafely {
                val detail = safeCall<RouteDetail?>(null) { getRouteDetail(code) } ?: return@launchSafely
                routeStops = detail.stops
                directionHeadsigns = detail.headsigns
                routeShapes = detail.shapes.toRouteDisplayModels()
                refreshDisplay()
            }
    }

    private fun startPositionsPolling(code: String) {
        positionsJob?.cancel()
        positionsJob =
            launchSafely {
                while (isActive) {
                    val buses =
                        safeCall<List<BusLocation>>(emptyList()) {
                            getRouteBusPositions.forRoute(code)
                        }
                    previousPositions = linePositions
                    linePositions = getRouteBusPositions.keepingTheFreshest(linePositions, buses)
                    refreshDisplay()
                    val followed = departure?.vehicleId
                    buses
                        .firstOrNull { it.vehicleId == followed || it.tripDuid == followed }
                        ?.shapeId
                        ?.let { loadTrackedShape(it) }
                    delay(TrackingConstants.POSITIONS_REFRESH_INTERVAL_MILLIS)
                }
            }
    }

    fun loadTrackedShape(shapeId: String?) {
        if (shapeId == null || shapeId == loadedTrackedShapeId) return
        trackedShapeJob?.cancel()
        trackedShapeJob =
            launchSafely {
                val points = getRouteShape(shapeId)
                if (points.isNotEmpty()) loadedTrackedShapeId = shapeId
                trackedShape = points.toRouteLineDisplayModel()
                refreshDisplay()
            }
    }

    private fun startArrivalsPolling(
        code: String,
        followedStopRef: String,
        followedAt: Instant?,
        followedVehicleRef: String?,
    ) {
        arrivalsJob?.cancel()
        arrivalsJob =
            launchSafely {
                while (isActive) {
                    val pinned =
                        safeCall(null) {
                            getPinnedArrivals(
                                code = code,
                                followedStopRef = followedStopRef,
                                followedAt = followedAt,
                                followedVehicleRef = followedVehicleRef,
                                pinned = pinnedTripRef,
                            )
                        }
                    if (pinned?.trip != null && pinned.predictions.isNotEmpty()) {
                        lineArrivals = pinned.predictions
                        pinnedTripRef = pinned.trip
                    }
                    if (!nextBusKnown && waitedEnough()) {
                        nextBusKnown = true
                        refreshDisplay()
                    }
                    delay(REFRESH_INTERVAL_MILLIS)
                }
            }
    }

    private fun startNextBusPolling(
        code: String,
        followedStopRef: String,
        followedAt: Instant?,
        followedVehicleId: String?,
        followedTripId: String?,
    ) {
        nextBusJob?.cancel()
        startedAtMs = nowEpochMilliseconds()
        nextBusJob =
            launchSafely {
                var polls = 0
                while (isActive) {
                    val next =
                        safeCall(null) {
                            val arrival =
                                getNextBusAtStop(
                                    lineCode = code,
                                    stopRef = followedStopRef,
                                    followedAt = followedAt,
                                    pinnedTripRef = pinnedTripRef,
                                    followedVehicleId = followedVehicleId,
                                    followedTripId = followedTripId,
                                )
                            val template = departure
                            val at = arrival?.expected
                            when {
                                arrival == null || at == null || template == null -> {
                                    null
                                }

                                else -> {
                                    template.copy(
                                        departTimestamp = at,
                                        delaySeconds = arrival.delaySeconds,
                                        vehicleId = arrival.vehicleRef ?: followedVehicleId,
                                        tripId = arrival.vehicleRef ?: followedTripId,
                                    )
                                }
                            }
                        }
                    nextBus = getNextBusAtStop.laterHour(nextBus, next)
                    polls++
                    nextBusKnown = next != null || (polls > 1 && waitedEnough())
                    refreshDisplay()
                    delay(REFRESH_INTERVAL_MILLIS)
                }
            }
    }

    fun markTripCompleted() {
        if (completed) return
        arrivalsJob?.cancel()
        nextBusJob?.cancel()
        completed = true
        pinnedTripRef = null
        refreshDisplay()
    }

    fun clearTrackedDeparture() {
        arrivalsJob?.cancel()
        nextBusJob?.cancel()
        positionsJob?.cancel()
        trackedShapeJob?.cancel()
        routeJob?.cancel()
        loadedTrackedShapeId = null
        routeStops = emptyList()
        directionHeadsigns = emptyList()
        routeShapes = emptyList()
        trackedShape = RouteLineDisplayModel()
        tripId = null
        stopRef = null
        departure = null
        lineArrivals = emptyMap()
        pinnedTripRef = null
        nextBus = null
        nextBusKnown = false
        linePositions = emptyList()
        previousPositions = emptyList()
        completed = false
        refreshDisplay()
        launchSafely { updateTrackedTrip(null) }
    }

    private fun waitedEnough(): Boolean =
        startedAtMs > 0 && nowEpochMilliseconds() - startedAtMs >= TrackingConstants.BUSES_NOT_LISTED_MILLIS

    private fun refreshDisplay() {
        val display = buildDisplay(routes.value.toRouteColors())
        logTrack(display)
        val vehicle = display.trackedBus?.vehicleId
        if (vehicle != null && followedBus?.vehicleId != vehicle) {
            followedBus = FollowedBus(rideGeneration, vehicle)
        }
        _uiState.update { it.copy(tracking = display) }
    }

    private fun logTrack(display: TrackingDisplayModel) {
        val signature =
            "${display.trackedBus?.vehicleId}#${display.busIndex}#${display.atStopRef}#" +
                "${display.riderStopState}#${display.trackedBus?.location?.latitude}," +
                display.trackedBus?.location?.longitude
        if (signature != lastLoggedSignature) {
            lastLoggedSignature = signature
            logDebug("track $signature")
        }
    }

    private fun buildDisplay(routeColors: Map<String, Color>): TrackingDisplayModel {
        val input =
            TrackingViewInput(
                routeStops = routeStops,
                directionHeadsigns = directionHeadsigns,
                departure = departure,
                tripId = tripId,
                stopRef = stopRef,
                positions = linePositions,
                previousPositions = previousPositions,
                predictions = lineArrivals,
                nowMs = nowEpochMilliseconds(),
                tripCompleted = completed,
                nextBus = nextBus,
                nextBusKnown = nextBusKnown,
                preferredVehicleId = preferredVehicleId(followedBus, rideGeneration),
            )
        return getTrackingView(input).toDisplayModel(
            routeColors = routeColors,
            trackedShape = trackedShape,
            routeShapes = routeShapes,
        )
    }

    data class UiState(
        val tracking: TrackingDisplayModel = TrackingDisplayModel(),
    )
}
