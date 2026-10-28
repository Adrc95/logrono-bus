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

package dev.adrc95.logronobus.core.network.builder

import dev.adrc95.logronobus.core.network.dto.ArrivalDto

class ArrivalDtoBuilder {
    var lineRef: String = "sample"
    var directionRef: String = "sample"
    var journeyCode: String = "sample"
    var vehicleRef: String = "sample"
    var stopPointRef: String = "sample"
    var order: Int? = null
    var aimedArrivalTime: String = "sample"
    var expectedArrivalTime: String = "sample"
    var aimedDepartureTime: String = "sample"
    var expectedDepartureTime: String = "sample"
    var arrivalStatus: String = "sample"
    var departureStatus: String = "sample"
    var cancellation: Boolean = false
    var predictionInaccurate: Boolean = false
    var delaySeconds: Int? = null

    fun withLineRef(lineRef: String) = apply { this.lineRef = lineRef }

    fun withDirectionRef(directionRef: String) = apply { this.directionRef = directionRef }

    fun withJourneyCode(journeyCode: String) = apply { this.journeyCode = journeyCode }

    fun withVehicleRef(vehicleRef: String) = apply { this.vehicleRef = vehicleRef }

    fun withStopPointRef(stopPointRef: String) = apply { this.stopPointRef = stopPointRef }

    fun withOrder(order: Int?) = apply { this.order = order }

    fun withAimedArrivalTime(aimedArrivalTime: String) = apply { this.aimedArrivalTime = aimedArrivalTime }

    fun withExpectedArrivalTime(expectedArrivalTime: String) = apply { this.expectedArrivalTime = expectedArrivalTime }

    fun withAimedDepartureTime(aimedDepartureTime: String) = apply { this.aimedDepartureTime = aimedDepartureTime }

    fun withExpectedDepartureTime(expectedDepartureTime: String) = apply { this.expectedDepartureTime = expectedDepartureTime }

    fun withArrivalStatus(arrivalStatus: String) = apply { this.arrivalStatus = arrivalStatus }

    fun withDepartureStatus(departureStatus: String) = apply { this.departureStatus = departureStatus }

    fun withCancellation(cancellation: Boolean) = apply { this.cancellation = cancellation }

    fun withPredictionInaccurate(predictionInaccurate: Boolean) = apply { this.predictionInaccurate = predictionInaccurate }

    fun withDelaySeconds(delaySeconds: Int?) = apply { this.delaySeconds = delaySeconds }

    fun build() =
        ArrivalDto(
            lineRef = lineRef,
            directionRef = directionRef,
            journeyCode = journeyCode,
            vehicleRef = vehicleRef,
            stopPointRef = stopPointRef,
            order = order,
            aimedArrivalTime = aimedArrivalTime,
            expectedArrivalTime = expectedArrivalTime,
            aimedDepartureTime = aimedDepartureTime,
            expectedDepartureTime = expectedDepartureTime,
            arrivalStatus = arrivalStatus,
            departureStatus = departureStatus,
            cancellation = cancellation,
            predictionInaccurate = predictionInaccurate,
            delaySeconds = delaySeconds,
        )
}

fun arrivalDto(block: ArrivalDtoBuilder.() -> Unit = {}) = ArrivalDtoBuilder().apply(block).build()
