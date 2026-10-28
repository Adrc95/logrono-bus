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

package dev.adrc95.logronobus.core.network.datasource

import dev.adrc95.logronobus.core.common.exception.Result
import dev.adrc95.logronobus.core.common.exception.map
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.LinePasses
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.service.DeparturesService
import dev.adrc95.logronobus.core.network.tryCall
import org.koin.core.annotation.Factory

@Factory(binds = [DeparturesRemoteDataSource::class])
class LogronoDeparturesDataSource(
    private val service: DeparturesService,
) : DeparturesRemoteDataSource {
    override suspend fun arrivals(
        stopId: Int,
        lines: List<Int>,
        previewMinutes: Int,
    ): Result<List<Arrival>> =
        tryCall { service.getArrivals(stopId, lines.joinToString(","), previewMinutes) }
            .map { response -> response.result.arrivals.map { it.toDomain() } }

    override suspend fun lineArrivals(
        lineId: Int,
        lines: List<Int>,
    ): Result<List<Arrival>> =
        tryCall { service.getLineArrivals(lineId, lines.joinToString(",")) }
            .map { response -> response.result.arrivals.map { it.toDomain() } }

    override suspend fun passes(
        stopId: Int,
        lines: List<Int>,
    ): Result<List<Arrival>> =
        tryCall { service.getPasses(stopId, lines.joinToString(",")) }
            .map { response -> response.result.passes.map { it.toDomain() } }

    override suspend fun linePasses(lineId: Int): Result<LinePasses> =
        tryCall { service.getLineTimetable(lineId) }
            .map { response ->
                LinePasses(
                    overall = response.result.passes.filter { it.isNotBlank() },
                    byDirection =
                        response.result.passesByDirection
                            .mapValues { (_, passes) -> passes.filter { it.isNotBlank() } }
                            .filterValues { it.isNotEmpty() },
                )
            }
}
