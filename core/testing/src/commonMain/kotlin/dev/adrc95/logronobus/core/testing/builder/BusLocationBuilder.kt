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

import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.StopPrediction
import kotlin.time.Instant

class BusLocationBuilder {
    var latitude: Double = 42.4
    var longitude: Double = -2.4
    var bearing: Float? = null
    var modifiedTimestamp: Instant = Instant.parse("2026-10-18T12:00:00Z")
    var tripDuid: String = "trip-1"
    var vehicleId: String? = "V1"
    var timetableId: String? = "4"
    var lineColor: String? = null
    var headsign: String? = null
    var nextStopRef: String? = null
    var nextStops: List<StopPrediction>? = null
    var shapeId: String? = null
    var directionIndex: Int? = null

    fun withLatitude(latitude: Double) = apply { this.latitude = latitude }

    fun withLongitude(longitude: Double) = apply { this.longitude = longitude }

    fun withBearing(bearing: Float?) = apply { this.bearing = bearing }

    fun withModifiedTimestamp(modifiedTimestamp: Instant) = apply { this.modifiedTimestamp = modifiedTimestamp }

    fun withTripDuid(tripDuid: String) = apply { this.tripDuid = tripDuid }

    fun withVehicleId(vehicleId: String?) = apply { this.vehicleId = vehicleId }

    fun withTimetableId(timetableId: String?) = apply { this.timetableId = timetableId }

    fun withLineColor(lineColor: String?) = apply { this.lineColor = lineColor }

    fun withHeadsign(headsign: String?) = apply { this.headsign = headsign }

    fun withNextStopRef(nextStopRef: String?) = apply { this.nextStopRef = nextStopRef }

    fun withNextStops(nextStops: List<StopPrediction>?) = apply { this.nextStops = nextStops }

    fun withShapeId(shapeId: String?) = apply { this.shapeId = shapeId }

    fun withDirectionIndex(directionIndex: Int?) = apply { this.directionIndex = directionIndex }

    fun build() =
        BusLocation(
            latitude = latitude,
            longitude = longitude,
            bearing = bearing,
            modifiedTimestamp = modifiedTimestamp,
            tripDuid = tripDuid,
            vehicleId = vehicleId,
            timetableId = timetableId,
            lineColor = lineColor,
            headsign = headsign,
            nextStopRef = nextStopRef,
            nextStops = nextStops,
            shapeId = shapeId,
            directionIndex = directionIndex,
        )
}

fun busLocation(block: BusLocationBuilder.() -> Unit = {}) = BusLocationBuilder().apply(block).build()
