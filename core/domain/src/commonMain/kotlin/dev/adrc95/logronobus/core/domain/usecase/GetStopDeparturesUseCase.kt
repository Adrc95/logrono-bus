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

package dev.adrc95.logronobus.core.domain.usecase

import dev.adrc95.logronobus.core.domain.DomainConstants.DEFAULT_WINDOW_MINUTES
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.model.StopDepartures
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class GetStopDeparturesUseCase(
    private val departures: DeparturesRepository,
) {
    suspend operator fun invoke(
        stopId: String,
        now: Instant? = null,
        windowMinutes: Int = DEFAULT_WINDOW_MINUTES,
        lineFilter: String? = null,
        directionFilter: Int? = null,
    ): StopDepartures =
        if (now == null) {
            departures.getStopDeparturesWithLive(
                stopId = stopId,
                windowMinutes = windowMinutes,
                lineFilter = lineFilter,
                directionFilter = directionFilter,
            )
        } else {
            departures.getStopDeparturesWithLive(
                stopId = stopId,
                now = now,
                windowMinutes = windowMinutes,
                lineFilter = lineFilter,
                directionFilter = directionFilter,
            )
        }
}
