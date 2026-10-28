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

package dev.adrc95.logronobus.core.data.vehicles

import dev.adrc95.logronobus.core.common.exception.getOrNull
import dev.adrc95.logronobus.core.data.DataConstants
import dev.adrc95.logronobus.core.domain.trip.chooseTheoreticalArrival
import dev.adrc95.logronobus.core.domain.trip.isNightLine
import dev.adrc95.logronobus.core.model.Arrival
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.StopPrediction
import dev.adrc95.logronobus.core.network.datasource.DeparturesRemoteDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class NightArrivalEstimator(
    private val remote: DeparturesRemoteDataSource,
) {
    private val mutex = Mutex()
    private var cache: Map<String, Pair<Long, List<Arrival>>> = emptyMap()

    suspend fun withTheoreticalArrivals(
        buses: BusPositions,
        now: Instant,
        lineIdOf: suspend (String) -> Int?,
    ): BusPositions {
        val blind =
            buses.byRoute
                .filterKeys(::isNightLine)
                .values
                .flatten()
                .any { it.nextStops?.singleOrNull()?.isBlind == true }
        if (!blind) return buses
        val filled =
            buses.byRoute.mapValues { (code, line) ->
                if (!isNightLine(code)) return@mapValues line
                line.map { bus -> bus.withTheoreticalArrival(code, now, lineIdOf) }
            }
        return buses.copy(byRoute = filled)
    }

    private suspend fun BusLocation.withTheoreticalArrival(
        code: String,
        now: Instant,
        lineIdOf: suspend (String) -> Int?,
    ): BusLocation {
        val prediction = nextStops?.singleOrNull()?.takeIf { it.isBlind } ?: return this
        val lineId = lineIdOf(code) ?: return this
        val stopId = prediction.stopRef.toIntOrNull() ?: return this
        val arrival =
            chooseTheoreticalArrival(
                passes = passesAt(lineId, stopId, now),
                sequence = prediction.stopSequence,
                now = now,
                delaySeconds = prediction.delay ?: 0,
            ) ?: return this
        return copy(
            nextStops =
                listOf(
                    prediction.copy(arrivalTimestamp = arrival, departureTimestamp = arrival),
                ),
        )
    }

    private suspend fun passesAt(
        lineId: Int,
        stopId: Int,
        now: Instant,
    ): List<Arrival> {
        val key = "$lineId:$stopId"
        val nowMs = now.toEpochMilliseconds()
        mutex
            .withLock { cache[key] }
            ?.takeIf { nowMs - it.first < DataConstants.THEORETICAL_ETA_TTL_MILLIS }
            ?.let { return it.second }
        val fetched = remote.passes(stopId, listOf(lineId)).getOrNull() ?: return emptyList()
        mutex.withLock { cache = cache + (key to (nowMs to fetched)) }
        return fetched
    }
}

private val StopPrediction.isBlind: Boolean
    get() = arrivalTimestamp == null && departureTimestamp == null
