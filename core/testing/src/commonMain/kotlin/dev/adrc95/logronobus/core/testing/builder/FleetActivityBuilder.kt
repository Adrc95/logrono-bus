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

import dev.adrc95.logronobus.core.model.FleetActivity
import kotlin.time.Instant

class FleetActivityBuilder {
    var vehicleRef: String = "V1"
    var lineRef: String = "4"
    var directionRef: String = "direction-4-1"
    var publishedLineName: String? = "Valdegastea"
    var latitude: Double = 42.4
    var longitude: Double = -2.4
    var recordedAt: Instant? = Instant.parse("2026-10-18T12:00:00Z")
    var delaySeconds: Int? = 0
    var predictionInaccurate: Boolean = false
    var nextStopRef: String? = "360"
    var nextStopExpectedAt: Instant? = Instant.parse("2026-10-18T12:05:00Z")
    var nextStopAimedAt: Instant? = Instant.parse("2026-10-18T12:05:00Z")

    fun withVehicleRef(vehicleRef: String) = apply { this.vehicleRef = vehicleRef }

    fun withLineRef(lineRef: String) = apply { this.lineRef = lineRef }

    fun withDirectionRef(directionRef: String) = apply { this.directionRef = directionRef }

    fun withPublishedLineName(publishedLineName: String?) = apply { this.publishedLineName = publishedLineName }

    fun withLatitude(latitude: Double) = apply { this.latitude = latitude }

    fun withLongitude(longitude: Double) = apply { this.longitude = longitude }

    fun withRecordedAt(recordedAt: Instant?) = apply { this.recordedAt = recordedAt }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun withPredictionInaccurate(predictionInaccurate: Boolean) = apply { this.predictionInaccurate = predictionInaccurate }

    fun withNextStopRef(nextStopRef: String?) = apply { this.nextStopRef = nextStopRef }

    fun withNextStopExpectedAt(nextStopExpectedAt: Instant?) = apply { this.nextStopExpectedAt = nextStopExpectedAt }

    fun withNextStopAimedAt(nextStopAimedAt: Instant?) = apply { this.nextStopAimedAt = nextStopAimedAt }

    fun build() =
        FleetActivity(
            vehicleRef = vehicleRef,
            lineRef = lineRef,
            directionRef = directionRef,
            publishedLineName = publishedLineName,
            latitude = latitude,
            longitude = longitude,
            recordedAt = recordedAt,
            delaySeconds = delaySeconds,
            predictionInaccurate = predictionInaccurate,
            nextStopRef = nextStopRef,
            nextStopExpectedAt = nextStopExpectedAt,
            nextStopAimedAt = nextStopAimedAt,
        )
}

fun fleetActivity(block: FleetActivityBuilder.() -> Unit = {}) = FleetActivityBuilder().apply(block).build()
