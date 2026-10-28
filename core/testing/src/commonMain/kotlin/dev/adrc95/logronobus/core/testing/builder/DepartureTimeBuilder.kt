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

package dev.adrc95.logronobus.core.testing.builder

import dev.adrc95.logronobus.core.model.DepartureTime
import kotlin.time.Instant

class DepartureTimeBuilder {
    var displayName: String = "Valdegastea"
    var timetableId: String = "4"
    var departTimestamp: Instant? = Instant.parse("2026-10-18T12:00:00Z")
    var delaySeconds: Int? = 0
    var tripId: String? = "trip-1"
    var vehicleId: String? = "trip-1"
    var headsign: String? = "Valdegastea"
    var directionIndex: Int? = null
    var arrivalStatus: String? = null
    var terminatesHere: Boolean = false
    var startsHere: Boolean = false
    var predictionInaccurate: Boolean = false

    fun withDisplayName(displayName: String) = apply { this.displayName = displayName }

    fun withTimetableId(timetableId: String) = apply { this.timetableId = timetableId }

    fun withDepartTimestamp(departTimestamp: Instant?) = apply { this.departTimestamp = departTimestamp }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun withTripId(tripId: String?) = apply { this.tripId = tripId }

    fun withVehicleId(vehicleId: String?) = apply { this.vehicleId = vehicleId }

    fun withHeadsign(headsign: String?) = apply { this.headsign = headsign }

    fun withDirectionIndex(directionIndex: Int?) = apply { this.directionIndex = directionIndex }

    fun withArrivalStatus(arrivalStatus: String?) = apply { this.arrivalStatus = arrivalStatus }

    fun withTerminatesHere(terminatesHere: Boolean) = apply { this.terminatesHere = terminatesHere }

    fun withStartsHere(startsHere: Boolean) = apply { this.startsHere = startsHere }

    fun withPredictionInaccurate(predictionInaccurate: Boolean) = apply { this.predictionInaccurate = predictionInaccurate }

    fun build() =
        DepartureTime(
            displayName = displayName,
            timetableId = timetableId,
            departTimestamp = departTimestamp,
            delaySeconds = delaySeconds,
            tripId = tripId,
            vehicleId = vehicleId,
            headsign = headsign,
            directionIndex = directionIndex,
            arrivalStatus = arrivalStatus,
            terminatesHere = terminatesHere,
            startsHere = startsHere,
            predictionInaccurate = predictionInaccurate,
        )
}

fun departure(block: DepartureTimeBuilder.() -> Unit = {}) = DepartureTimeBuilder().apply(block).build()
