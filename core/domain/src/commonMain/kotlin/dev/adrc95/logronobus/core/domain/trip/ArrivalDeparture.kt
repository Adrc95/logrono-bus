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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.DepartureTime
import kotlin.math.roundToInt
import kotlin.time.Instant

fun Arrival.toDeparture(
    headsign: String?,
    now: Instant,
    windowMinutes: Int,
): DepartureTime? {
    if (cancellation) return null
    val code = lineRef.trim()
    if (code.isEmpty()) return null

    val expected = expected ?: return null
    if (!isBoardable(expected, now, windowMinutes)) return null

    val delay =
        delaySeconds
            ?: aimed?.let { ((expected - it).inWholeSeconds / 60.0).roundToInt() * 60 }

    return DepartureTime(
        displayName = headsign.orEmpty(),
        timetableId = code,
        departTimestamp = expected,
        delaySeconds = delay,
        tripId = vehicleRef,
        vehicleId = vehicleRef,
        providerTripId = vehicleRef,
        headsign = headsign,
        arrivalStatus = arrivalStatus,
        terminatesHere = terminatesHere,
        startsHere = startsHere,
        predictionInaccurate = predictionInaccurate,
    )
}
