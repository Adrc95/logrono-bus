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

package dev.adrc95.logronobus.core.domain.departure

import dev.adrc95.logronobus.core.model.DepartureTime

fun terminatesHere(
    departure: DepartureTime,
    stopName: String?,
): Boolean =
    departure.terminatesHere ||
        (!stopName.isNullOrBlank() && departure.headsign == stopName)

val DepartureTime.isLive: Boolean
    get() =
        delaySeconds != null &&
            (delaySeconds != 0 || arrivalStatus?.lowercase() !in TIMETABLE_ONLY_STATUSES)

val DepartureTime.hasMeasuredDelay: Boolean
    get() =
        delaySeconds != null &&
            !predictionInaccurate &&
            arrivalStatus?.lowercase() !in TIMETABLE_ONLY_STATUSES

private val TIMETABLE_ONLY_STATUSES = setOf("scheduled", "no_report")
