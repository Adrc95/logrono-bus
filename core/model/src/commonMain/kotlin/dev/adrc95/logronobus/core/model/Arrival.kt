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

data class Arrival(
    val lineRef: String,
    val directionRef: String,
    val order: Int?,
    val aimedArrival: Instant?,
    val expectedArrival: Instant?,
    val aimedDeparture: Instant?,
    val expectedDeparture: Instant?,
    val vehicleRef: String?,
    val stopRef: String? = null,
    val cancellation: Boolean,
    val predictionInaccurate: Boolean,
    val delaySeconds: Int?,
    val arrivalStatus: String?,
) {
    val expected: Instant? get() = expectedDeparture ?: expectedArrival
    val aimed: Instant? get() = aimedDeparture ?: aimedArrival
    val terminatesHere: Boolean get() = departureMissing && !arrivalMissing
    val startsHere: Boolean get() = arrivalMissing && !departureMissing
    private val arrivalMissing: Boolean get() = expectedArrival == null && aimedArrival == null
    private val departureMissing: Boolean get() = expectedDeparture == null && aimedDeparture == null
}
