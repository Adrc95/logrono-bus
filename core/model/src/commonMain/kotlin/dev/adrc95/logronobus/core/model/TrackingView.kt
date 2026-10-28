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

package dev.adrc95.logronobus.core.model

import kotlin.time.Instant

data class TrackingView(
    val directionIndex: Int = 0,
    val followedBus: BusLocation? = null,
    val trackedBus: BusLocation? = null,
    val busIndex: Int = -1,
    val atStopRef: String? = null,
    val nextStopRef: String? = null,
    val stopEtas: Map<String, StopEta> = emptyMap(),
    val nextEta: Pair<String, StopEta>? = null,
    val passedAt: Instant? = null,
    val riderStopState: RiderStopState = RiderStopState.Unknown,
    val completed: Boolean = false,
    val tripId: String? = null,
    val timelineStops: List<Stop> = emptyList(),
    val riderStopRef: String? = null,
    val riderStopName: String? = null,
    val departure: DepartureTime? = null,
    val nextBus: DepartureTime? = null,
    val nextBusKnown: Boolean = false,
    val atYourStop: Boolean = false,
)
