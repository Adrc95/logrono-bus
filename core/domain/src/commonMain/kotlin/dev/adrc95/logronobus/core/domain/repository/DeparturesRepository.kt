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

package dev.adrc95.logronobus.core.domain.repository

import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.domain.DomainConstants.DEFAULT_WINDOW_MINUTES
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.model.StopDepartures
import kotlin.time.Instant

interface DeparturesRepository {
    suspend fun learnedDirections(): Map<String, Map<String, String>>

    suspend fun rememberDirection(
        code: String,
        directionRef: String,
        terminal: String,
    )

    suspend fun getLineArrivals(code: String): List<Arrival>

    suspend fun getStopDeparturesWithLive(
        stopId: String,
        now: Instant = Instant.fromEpochMilliseconds(nowEpochMilliseconds()),
        windowMinutes: Int = DEFAULT_WINDOW_MINUTES,
        lineFilter: String? = null,
        directionFilter: Int? = null,
    ): StopDepartures

    suspend fun getStopLineStarts(
        stopId: String,
        now: Instant = Instant.fromEpochMilliseconds(nowEpochMilliseconds()),
        lineFilter: String? = null,
        directionFilter: Int? = null,
    ): List<DepartureTime>
}
