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

package dev.adrc95.logronobus.feature.nearby

import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.domain.usecase.GetCurrentLocationUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFavouriteStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetNearbyStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRoutesUseCase
import dev.adrc95.logronobus.core.domain.usecase.ToggleFavouriteUseCase
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.LocationResult
import dev.adrc95.logronobus.core.model.NearbyStop
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.toRouteColors
import dev.adrc95.logronobus.core.ui.util.toRouteOrigins
import dev.adrc95.logronobus.feature.nearby.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class NearbyViewModel(
    private val getNearbyStops: GetNearbyStopsUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val getFavourites: GetFavouriteStopsUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
    private val getRoutes: GetRoutesUseCase,
) : ViewModel() {
    private var domainNearby: List<NearbyStop> = emptyList()
    private val _uiState = MutableStateFlow(UiState())

    private val favourites: StateFlow<List<FavouriteStop>> =
        getFavourites()
            .stateInViewModel(this, emptyList())

    private val routes: StateFlow<List<Route>> =
        flow {
            emit(safeCall(emptyList()) { getRoutes().values.toList() })
        }.stateInViewModel(this, emptyList())

    private val reloads = MutableStateFlow(0)

    private val loader: StateFlow<Int> =
        reloads
            .onEach { loadNearby() }
            .stateInViewModel(this, 0)

    val uiState: StateFlow<UiState> =
        combine(
            favourites,
            routes,
            loader,
            _uiState,
        ) { favourites, routes, _, state ->
            val favouriteRefs = favourites.map { it.stopRef }.toSet()
            val coloured =
                domainNearby.toDisplayModel(
                    favouriteRefs = favouriteRefs,
                    isFallback = state.nearbyState == NearbyState.Unavailable,
                    routeColors = routes.toRouteColors(),
                    routeOrigins = routes.toRouteOrigins(),
                )
            state.copy(
                favouriteRefs = favouriteRefs,
                nearby = if (domainNearby.isEmpty()) state.nearby else coloured,
            )
        }.stateInViewModel(this, UiState())

    fun toggleFavourite(stop: StopDisplayModel) {
        val already = favourites.value.any { it.stopRef == stop.stopRef }
        launchSafely {
            domainNearby.firstOrNull { it.stop.stopRef == stop.stopRef }?.let {
                safeCall(Unit) { toggleFavouriteUseCase(it.stop, already) }
            }
        }
    }

    fun reload() {
        reloads.update { it + 1 }
    }

    private fun loadNearby() {
        launchSafely {
            _uiState.update { it.copy(nearbyState = NearbyState.Loading) }
            val colors = routes.value.toRouteColors()
            val origins = routes.value.toRouteOrigins()
            when (val result = getCurrentLocation()) {
                is LocationResult.Available -> {
                    val nearby =
                        safeCall<List<NearbyStop>?>(null) {
                            getNearbyStops.nearbyStops(result.location, NearbyConstants.STOP_LIMIT)
                        } ?: run {
                            _uiState.update { it.copy(nearbyState = NearbyState.Unavailable) }
                            return@launchSafely
                        }
                    val withWalks =
                        safeCall(nearby) {
                            getNearbyStops.withWalkingTimes(result.location, nearby, NearbyConstants.WALK_LIMIT)
                        }
                    domainNearby = withWalks
                    _uiState.update {
                        it.copy(
                            nearby =
                                withWalks.toDisplayModel(
                                    favouriteRefs = it.favouriteRefs,
                                    isFallback = false,
                                    routeColors = colors,
                                    routeOrigins = origins,
                                ),
                            nearbyState = NearbyState.Located(result.location.toDisplayModel()),
                        )
                    }
                }

                LocationResult.PermissionDenied -> {
                    _uiState.update { it.copy(nearbyState = NearbyState.PermissionDenied) }
                }

                LocationResult.Unavailable -> {
                    val nearby =
                        safeCall<List<NearbyStop>?>(null) {
                            getNearbyStops.nearbyStops(NearbyConstants.LOGRONO_CENTRE, NearbyConstants.STOP_LIMIT)
                        }.orEmpty()
                    domainNearby = nearby
                    _uiState.update {
                        it.copy(
                            nearby =
                                nearby.toDisplayModel(
                                    favouriteRefs = it.favouriteRefs,
                                    isFallback = true,
                                    routeColors = colors,
                                    routeOrigins = origins,
                                ),
                            nearbyState = NearbyState.Unavailable,
                        )
                    }
                }
            }
        }
    }

    data class UiState(
        val nearbyState: NearbyState = NearbyState.Idle,
        val nearby: NearbyDisplayModel =
            NearbyDisplayModel(
                stops = persistentListOf(),
                isFallback = false,
            ),
        val favouriteRefs: Set<String> = emptySet(),
    )
}
