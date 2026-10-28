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

package dev.adrc95.logronobus.core.testing

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

internal val STOP_TIMES_JSON: String =
    buildString {
        append("""{"result":{"arrivals":[""")
        append(
            (0 until 10).joinToString(",") { i ->
                val at = Clock.System.now() + ((i + 1) * 300L).seconds
                val local = at.toLocalStamp()
                """{"lineRef":"6","directionRef":"","journeyCode":"J$i","vehicleRef":"$i","stopPointRef":"152","order":11,"aimedArrivalTime":"$local","expectedArrivalTime":"$local","aimedDepartureTime":"$local","expectedDepartureTime":"$local","arrivalStatus":"scheduled","departureStatus":"scheduled","cancellation":false,"predictionInaccurate":false,"delaySeconds":0}"""
            },
        )
        append("]}}")
    }
