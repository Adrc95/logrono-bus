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

data class BusLocation(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float? = null,
    val modifiedTimestamp: Instant,
    val tripDuid: String,
    val vehicleId: String? = null,
    val timetableId: String? = null,
    val lineColor: String? = null,
    val headsign: String? = null,
    val nextStopRef: String? = null,
    val nextStops: List<StopPrediction>? = null,
    val shapeId: String? = null,
    val directionIndex: Int? = null,
)
