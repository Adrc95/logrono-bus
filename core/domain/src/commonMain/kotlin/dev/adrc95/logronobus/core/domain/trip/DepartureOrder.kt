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

import dev.adrc95.logronobus.core.common.time.parseInstant
import dev.adrc95.logronobus.core.domain.DomainConstants.SAME_TRIP_TOLERANCE_SECONDS
import dev.adrc95.logronobus.core.model.DepartureTime
import kotlin.math.abs
import kotlin.time.Instant

fun isSameTrip(
    at: Instant?,
    bt: Instant?,
): Boolean = !(at == null || bt == null) && abs((at - bt).inWholeSeconds) <= SAME_TRIP_TOLERANCE_SECONDS

fun clocksBefore(
    mine: Instant?,
    theirs: Instant?,
): Boolean = !(mine == null || theirs == null) && mine < theirs

fun departureInstantOf(departure: DepartureTime): Instant? = departure.departTimestamp

fun inOrder(rows: List<DepartureTime>): List<DepartureTime> =
    rows.sortedWith(
        compareBy { departureInstantOf(it) ?: Instant.DISTANT_PAST },
    )
