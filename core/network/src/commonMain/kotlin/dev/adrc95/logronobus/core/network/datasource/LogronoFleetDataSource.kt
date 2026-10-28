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
import dev.adrc95.logronobus.core.common.platform.answerClockMillisecondsOrNull
import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.BusLocation
import dev.adrc95.logronobus.core.model.BusPositions
import dev.adrc95.logronobus.core.model.FleetActivity
import dev.adrc95.logronobus.core.model.FleetFeed
import dev.adrc95.logronobus.core.model.GeoPoint
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopPrediction
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.core.network.service.FleetService
import dev.adrc95.logronobus.core.network.tryCall
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import kotlin.time.Instant

@Single(binds = [FleetDataSource::class])
class LogronoFleetDataSource(
    private val service: FleetService,
    private val catalogue: CatalogueDataSource,
) : FleetDataSource {
    private val mutex = Mutex()
    private var cache: Pair<Long, Result<FleetFeed>>? = null
    private var lineCache: Map<Int, Pair<Long, FleetFeed>> = emptyMap()
    private var positionCache: Map<String, Pair<Long, BusPositions>> = emptyMap()

    override suspend fun feed(
        now: Instant,
        forceRefresh: Boolean,
    ): Result<FleetFeed> =
        mutex.withLock {
            val cached =
                cache?.takeIf {
                    !forceRefresh &&
                        now.toEpochMilliseconds() - it.first <
                        NetworkConstants.VEHICLE_CACHE_TTL_MILLIS
                }
            if (cached != null) {
                return@withLock when (val answer = cached.second) {
                    is Result.Success -> Result.Success(answer.data.copy(serverTimeMs = null))
                    is Result.Error -> answer
                }
            }

            when (val answer = tryCall { service.getFleet() }) {
                is Result.Success -> {
                    val feed =
                        FleetFeed(
                            activities =
                                answer.data.result.activities
                                    .map { it.toDomain() },
                            serverTimeMs = answerClockMillisecondsOrNull(),
                        )
                    cache = now.toEpochMilliseconds() to Result.Success(feed)
                    Result.Success(feed)
                }

                is Result.Error -> {
                    cache = now.toEpochMilliseconds() to answer
                    answer
                }
            }
        }

    override suspend fun positions(
        now: Instant,
        forceRefresh: Boolean,
    ): Result<BusPositions> =
        when (val answer = feed(now, forceRefresh)) {
            is Result.Success -> Result.Success(answer.data.drawnAt(now))
            is Result.Error -> answer
        }

    override suspend fun positionsFor(
        routeNum: String,
        now: Instant,
        forceRefresh: Boolean,
    ): Result<BusPositions> {
        if (catalogue.internalIdOf(routeNum) == null) {
            return when (val answer = feed(now, forceRefresh)) {
                is Result.Success -> {
                    val all = answer.data.drawnAt(now)
                    Result.Success(
                        BusPositions(
                            byRoute = mapOf(routeNum to all.forRoute(routeNum)),
                            stale = all.stale,
                            staleSeconds = all.staleSeconds,
                        ),
                    )
                }

                is Result.Error -> {
                    answer
                }
            }
        }
        val nowMs = now.toEpochMilliseconds()
        if (!forceRefresh) {
            cachedPositions(routeNum, nowMs)?.let { return Result.Success(it) }
        }
        return when (val answer = positionsForLine(routeNum, now)) {
            is Result.Success -> {
                rememberPositions(routeNum, nowMs, answer.data)
                answer
            }

            is Result.Error -> {
                lastPositions(routeNum)
                    ?.takeIf { nowMs - it.first <= NetworkConstants.STALE_POSITIONS_MAX_AGE_MILLIS }
                    ?.let { (at, positions) ->
                        Result.Success(
                            positions.copy(stale = true, staleSeconds = (nowMs - at) / 1000),
                        )
                    } ?: answer
            }
        }
    }

    private suspend fun lastPositions(routeNum: String): Pair<Long, BusPositions>? =
        mutex.withLock { positionCache[routeNum] }

    private suspend fun cachedPositions(
        routeNum: String,
        nowMs: Long,
    ): BusPositions? =
        mutex.withLock {
            positionCache[routeNum]
                ?.takeIf { nowMs - it.first < NetworkConstants.VEHICLE_CACHE_TTL_MILLIS }
                ?.second
        }

    private suspend fun rememberPositions(
        routeNum: String,
        nowMs: Long,
        positions: BusPositions,
    ) = mutex.withLock {
        positionCache = positionCache + (routeNum to (nowMs to positions))
    }

    override suspend fun positionsForLine(
        lineCode: String,
        now: Instant,
    ): Result<BusPositions> {
        val internalId =
            catalogue.internalIdOf(lineCode)
                ?: return Result.Success(BusPositions(byRoute = emptyMap()))

        return when (val answer = tryCall { service.getLineFleet(internalId) }) {
            is Result.Success -> {
                val feed =
                    FleetFeed(
                        activities =
                            answer.data.result.activities
                                .map { it.toDomain() },
                        serverTimeMs = answerClockMillisecondsOrNull(),
                    )
                val all = feed.drawnAt(now)
                Result.Success(
                    BusPositions(
                        byRoute = mapOf(lineCode to all.forRoute(lineCode)),
                        stale = all.stale,
                        staleSeconds = all.staleSeconds,
                    ),
                )
            }

            is Result.Error -> {
                answer
            }
        }
    }

    override suspend fun feedForLines(
        internalLines: List<Int>,
        now: Instant,
    ): Result<FleetFeed> {
        if (internalLines.isEmpty()) {
            return Result.Success(FleetFeed(emptyList(), null))
        }
        if (internalLines.size > NetworkConstants.MAX_LINES_PER_REQUEST) {
            return feed(
                now,
                forceRefresh = false,
            )
        }
        val nowMs = now.toEpochMilliseconds()
        val cached = freshLines(nowMs)
        val missing = internalLines.distinct().filter { it !in cached }
        val loaded = mutableMapOf<Int, FleetFeed>()
        if (missing.isNotEmpty()) {
            val answers =
                coroutineScope {
                    missing
                        .map { id ->
                            async {
                                id to
                                    when (val answer = tryCall { service.getLineFleet(id) }) {
                                        is Result.Success -> {
                                            Result.Success(
                                                FleetFeed(
                                                    activities =
                                                        answer.data.result.activities
                                                            .map { it.toDomain() },
                                                    serverTimeMs = answerClockMillisecondsOrNull(),
                                                ),
                                            )
                                        }

                                        is Result.Error -> {
                                            answer
                                        }
                                    }
                            }
                        }.awaitAll()
                }
            for ((id, answer) in answers) {
                when (answer) {
                    is Result.Success -> loaded[id] = answer.data
                    is Result.Error -> return answer
                }
            }
            rememberLines(nowMs, loaded)
        }

        val merged = mutableListOf<FleetActivity>()
        var serverTimeMs: Long? = null
        for (id in internalLines.distinct()) {
            val lineFeed = loaded[id] ?: cached[id] ?: continue
            merged += lineFeed.activities
            serverTimeMs = serverTimeMs ?: lineFeed.serverTimeMs
        }
        return Result.Success(FleetFeed(activities = merged, serverTimeMs = serverTimeMs))
    }

    private suspend fun freshLines(nowMs: Long): Map<Int, FleetFeed> =
        mutex.withLock {
            lineCache
                .mapNotNull { (id, entry) ->
                    entry.second
                        .takeIf { nowMs - entry.first < NetworkConstants.VEHICLE_CACHE_TTL_MILLIS }
                        ?.let { id to it }
                }.toMap()
        }

    private suspend fun rememberLines(
        nowMs: Long,
        loaded: Map<Int, FleetFeed>,
    ) = mutex.withLock {
        lineCache = lineCache + loaded.mapValues { nowMs to it.value }
    }

    private suspend fun FleetFeed.drawnAt(now: Instant): BusPositions {
        val clock = serverTimeMs?.let { Instant.fromEpochMilliseconds(it) } ?: now
        val delays =
            activities
                .mapNotNull { activity ->
                    activity.delaySeconds?.let {
                        activity.vehicleRef to it
                    }
                }.toMap()
        val feed =
            activities
                .toBusPositions(clock, codeFor = catalogue.fleetCodeResolver())
                .withGuessedNextStops(delays)
        val colours = catalogue.lineColours()
        if (colours.isEmpty()) return feed
        return feed.copy(
            byRoute =
                feed.byRoute.mapValues { (code, buses) ->
                    val colour = colours[code] ?: return@mapValues buses
                    buses.map { it.copy(lineColor = colour) }
                },
        )
    }

    private suspend fun BusPositions.withGuessedNextStops(delays: Map<String, Int>): BusPositions {
        if (byRoute.values.none { buses -> buses.any { it.nextStopRef == null } }) return this
        val directionsByLine = mutableMapOf<String, List<List<Stop>>>()
        val fixed =
            byRoute.mapValues { (code, buses) ->
                buses.map { bus ->
                    if (bus.nextStopRef != null) return@map bus
                    val directions =
                        directionsByLine.getOrPut(code) {
                            runCatching {
                                catalogue.getStopsForRoute(code)
                            }.getOrDefault(emptyList())
                        }
                    val next = nextStopAhead(directions, bus) ?: return@map bus
                    bus.copy(
                        nextStopRef = next.first.stopRef,
                        nextStops =
                            listOf(
                                StopPrediction(
                                    stopRef = next.first.stopRef,
                                    stopSequence = next.second,
                                    delay = delays[bus.tripDuid],
                                ),
                            ),
                    )
                }
            }
        return copy(byRoute = fixed)
    }
}

private fun FleetActivity.toBusLocation(
    now: Instant,
    codeFor: (lineRef: String, publishedName: String?) -> String?,
    maxAgeMinutes: Long = NetworkConstants.DEFAULT_FRESH_MINUTES,
): BusLocation? {
    val code = codeFor(lineRef, publishedLineName) ?: return null
    val recorded = recordedAt ?: return null
    if (latitude == 0.0 && longitude == 0.0) return null
    if (code.isEmpty() || vehicleRef.isBlank()) return null
    if ((now - recorded).inWholeMinutes > maxAgeMinutes) return null
    val nextRef = nextStopRef
    val nextAt = nextStopExpectedAt
    val ahead =
        if (nextRef != null && nextAt != null) {
            listOf(
                StopPrediction(
                    stopRef = nextRef,
                    stopSequence = 0,
                    arrivalTimestamp = nextAt,
                    departureTimestamp = nextAt,
                    delay = delaySeconds,
                ),
            )
        } else {
            null
        }

    return BusLocation(
        latitude = latitude,
        longitude = longitude,
        bearing = null,
        modifiedTimestamp = recorded,
        tripDuid = vehicleRef,
        vehicleId = vehicleRef,
        timetableId = code,
        headsign = publishedLineName,
        nextStopRef = nextStopRef,
        nextStops = ahead,
        shapeId = null,
        directionIndex = directionIndex(),
    )
}

private fun FleetActivity.directionIndex(): Int? =
    when {
        directionRef.equals(NetworkConstants.DIRECTION_REF_ASC, ignoreCase = true) -> 0
        directionRef.equals(NetworkConstants.DIRECTION_REF_DESC, ignoreCase = true) -> 1
        else -> null
    }

private fun List<FleetActivity>.toBusPositions(
    now: Instant,
    codeFor: (lineRef: String, publishedName: String?) -> String?,
    maxAgeMinutes: Long = NetworkConstants.DEFAULT_FRESH_MINUTES,
): BusPositions {
    val byRoute =
        mapNotNull { it.toBusLocation(now, codeFor, maxAgeMinutes) }
            .groupBy { it.timetableId ?: "" }
            .filterKeys { it.isNotEmpty() }
    return BusPositions(byRoute = byRoute, stale = false, staleSeconds = null)
}

private fun nextStopAhead(
    directions: List<List<Stop>>,
    bus: BusLocation,
): Pair<Stop, Int>? {
    val busPoint = GeoPoint(bus.latitude, bus.longitude)
    var best: Pair<Double, Pair<Stop, Int>>? = null
    for (stops in directions) {
        for (index in 0 until stops.lastIndex) {
            val from = GeoPoint(stops[index].latitude, stops[index].longitude)
            val to = GeoPoint(stops[index + 1].latitude, stops[index + 1].longitude)
            val distance = distanceToSegment(busPoint, from, to)
            if (best == null || distance < best.first) {
                best = distance to (stops[index + 1] to (index + 1))
            }
        }
    }
    return best?.second
}

private fun distanceToSegment(
    point: GeoPoint,
    from: GeoPoint,
    to: GeoPoint,
): Double {
    val dx = to.lon - from.lon
    val dy = to.lat - from.lat
    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0.0) return point.distanceTo(from)
    val t =
        (((point.lon - from.lon) * dx + (point.lat - from.lat) * dy) / lengthSquared)
            .coerceIn(0.0, 1.0)
    return point.distanceTo(GeoPoint(from.lat + t * dy, from.lon + t * dx))
}
