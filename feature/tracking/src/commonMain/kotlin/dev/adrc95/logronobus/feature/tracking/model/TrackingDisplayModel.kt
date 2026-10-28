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

package dev.adrc95.logronobus.feature.tracking.model

import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.model.RiderStopState
import dev.adrc95.logronobus.core.ui.model.BusLocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.RouteLineDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopEtaDisplayModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

data class TrackedDepartureDisplayModel(
    val displayName: String,
    val timetableId: String? = null,
    val departTimestamp: Instant? = null,
    val lineColor: Color? = null,
)

data class NextBusDisplayModel(
    val departTimestamp: Instant? = null,
    val delay: DelayStatus = DelayStatus.Unknown,
)

data class TrackingDisplayModel(
    val departure: TrackedDepartureDisplayModel? = null,
    val nextBus: NextBusDisplayModel? = null,
    val nextBusKnown: Boolean = false,
    val timelineStops: ImmutableList<StopDisplayModel> = persistentListOf(),
    val positions: ImmutableList<BusLocationDisplayModel> = persistentListOf(),
    val trackedBus: BusLocationDisplayModel? = null,
    val trackedTripId: String? = null,
    val trackedStopRef: String? = null,
    val delay: DelayStatus = DelayStatus.Unknown,
    val riderStopState: RiderStopState = RiderStopState.Unknown,
    val atStopRef: String? = null,
    val completed: Boolean = false,
    val stopEtas: Map<String, StopEtaDisplayModel> = emptyMap(),
    val mapEtas: Map<String, StopEtaDisplayModel> = emptyMap(),
    val busIndex: Int = -1,
    val mapPolyline: ImmutableList<RouteLineDisplayModel> = persistentListOf(),
    val riderStopName: String? = null,
    val atYourStop: Boolean = false,
    val passedAt: String? = null,
    val nextStopRef: String? = null,
)

data class FollowedBus(
    val rideGeneration: Int,
    val vehicleId: String,
)

fun preferredVehicleId(
    followed: FollowedBus?,
    rideGeneration: Int,
): String? = followed?.takeIf { it.rideGeneration == rideGeneration }?.vehicleId
