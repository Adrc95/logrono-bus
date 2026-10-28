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

import dev.adrc95.logronobus.core.model.Arrival
import kotlin.time.Instant

class ArrivalBuilder {
    var lineRef: String = "sample"
    var directionRef: String = "sample"
    var order: Int? = null
    var aimedArrival: Instant? = null
    var expectedArrival: Instant? = null
    var aimedDeparture: Instant? = null
    var expectedDeparture: Instant? = null
    var vehicleRef: String? = null
    var stopRef: String? = null
    var cancellation: Boolean = false
    var predictionInaccurate: Boolean = false
    var delaySeconds: Int? = null
    var arrivalStatus: String? = null

    fun withLineRef(lineRef: String) = apply { this.lineRef = lineRef }

    fun withDirectionRef(directionRef: String) = apply { this.directionRef = directionRef }

    fun withOrder(order: Int?) = apply { this.order = order }

    fun withAimedArrival(aimedArrival: Instant?) = apply { this.aimedArrival = aimedArrival }

    fun withExpectedArrival(expectedArrival: Instant?) = apply { this.expectedArrival = expectedArrival }

    fun withAimedDeparture(aimedDeparture: Instant?) = apply { this.aimedDeparture = aimedDeparture }

    fun withExpectedDeparture(expectedDeparture: Instant?) = apply { this.expectedDeparture = expectedDeparture }

    fun withVehicleRef(vehicleRef: String?) = apply { this.vehicleRef = vehicleRef }

    fun withStopRef(stopRef: String?) = apply { this.stopRef = stopRef }

    fun withCancellation(cancellation: Boolean) = apply { this.cancellation = cancellation }

    fun withPredictionInaccurate(predictionInaccurate: Boolean) = apply { this.predictionInaccurate = predictionInaccurate }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun withArrivalStatus(arrivalStatus: String?) = apply { this.arrivalStatus = arrivalStatus }

    fun build() =
        Arrival(
            lineRef = lineRef,
            directionRef = directionRef,
            order = order,
            aimedArrival = aimedArrival,
            expectedArrival = expectedArrival,
            aimedDeparture = aimedDeparture,
            expectedDeparture = expectedDeparture,
            vehicleRef = vehicleRef,
            stopRef = stopRef,
            cancellation = cancellation,
            predictionInaccurate = predictionInaccurate,
            delaySeconds = delaySeconds,
            arrivalStatus = arrivalStatus,
        )
}

fun arrival(block: ArrivalBuilder.() -> Unit = {}) = ArrivalBuilder().apply(block).build()
