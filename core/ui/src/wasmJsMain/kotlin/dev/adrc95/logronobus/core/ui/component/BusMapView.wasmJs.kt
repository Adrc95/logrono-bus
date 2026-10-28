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

package dev.adrc95.logronobus.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.ui.map.MapLibreWebBusMapView
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import kotlinx.collections.immutable.ImmutableList

@Composable
actual fun BusMapView(
    modifier: Modifier,
    positions: ImmutableList<BusLocationDisplayModel>,
    stops: ImmutableList<StopDisplayModel>,
    trackedTripId: String?,
    trackedStopRef: String?,
    trackedNextStopRef: String?,
    onStopClick: InputAction<StopDisplayModel>?,
    onBusClick: InputAction<BusLocationDisplayModel>?,
    userLocation: LocationDisplayModel?,
    polylines: List<RouteLineDisplayModel>,
    stopEtas: Map<String, StopEtaDisplayModel>,
    followBus: Boolean,
    routeColor: Color?,
) {
    MapLibreWebBusMapView(
        modifier = modifier,
        positions = positions,
        stops = stops,
        trackedTripId = trackedTripId,
        trackedStopRef = trackedStopRef,
        trackedNextStopRef = trackedNextStopRef,
        onStopClick = onStopClick,
        onBusClick = onBusClick,
        userLocation = userLocation,
        polylines = polylines,
        stopEtas = stopEtas,
        followBus = followBus,
        routeColor = routeColor,
    )
}
