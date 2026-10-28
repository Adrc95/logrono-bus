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

import dev.adrc95.logronobus.core.model.StopPrediction
import kotlin.time.Instant

class StopPredictionBuilder {
    var stopRef: String = "360"
    var stopSequence: Int = 1
    var arrivalTimestamp: Instant? = Instant.parse("2026-10-18T12:00:00Z")
    var departureTimestamp: Instant? = Instant.parse("2026-10-18T12:00:00Z")
    var delay: Int? = 0

    fun withStopRef(stopRef: String) = apply { this.stopRef = stopRef }

    fun withStopSequence(stopSequence: Int) = apply { this.stopSequence = stopSequence }

    fun withArrivalTimestamp(arrivalTimestamp: Instant?) = apply { this.arrivalTimestamp = arrivalTimestamp }

    fun withDepartureTimestamp(departureTimestamp: Instant?) = apply { this.departureTimestamp = departureTimestamp }

    fun withDelay(delay: Int?) = apply { this.delay = delay }

    fun build() =
        StopPrediction(
            stopRef = stopRef,
            stopSequence = stopSequence,
            arrivalTimestamp = arrivalTimestamp,
            departureTimestamp = departureTimestamp,
            delay = delay,
        )
}

fun stopPrediction(block: StopPredictionBuilder.() -> Unit = {}) = StopPredictionBuilder().apply(block).build()
