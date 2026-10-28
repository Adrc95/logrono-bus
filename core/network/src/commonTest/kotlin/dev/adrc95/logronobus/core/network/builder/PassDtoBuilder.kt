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

import dev.adrc95.logronobus.core.network.dto.PassDto

class PassDtoBuilder {
    var lineRef: String = "sample"
    var stopPointRef: String = "sample"
    var journeyCode: String = "sample"
    var order: Int = 0
    var aimedArrivalTime: String = "sample"
    var aimedDepartureTime: String = "sample"

    fun withLineRef(lineRef: String) = apply { this.lineRef = lineRef }

    fun withStopPointRef(stopPointRef: String) = apply { this.stopPointRef = stopPointRef }

    fun withJourneyCode(journeyCode: String) = apply { this.journeyCode = journeyCode }

    fun withOrder(order: Int) = apply { this.order = order }

    fun withAimedArrivalTime(aimedArrivalTime: String) = apply { this.aimedArrivalTime = aimedArrivalTime }

    fun withAimedDepartureTime(aimedDepartureTime: String) = apply { this.aimedDepartureTime = aimedDepartureTime }

    fun build() =
        PassDto(
            lineRef = lineRef,
            stopPointRef = stopPointRef,
            journeyCode = journeyCode,
            order = order,
            aimedArrivalTime = aimedArrivalTime,
            aimedDepartureTime = aimedDepartureTime,
        )
}

fun passDto(block: PassDtoBuilder.() -> Unit = {}) = PassDtoBuilder().apply(block).build()
