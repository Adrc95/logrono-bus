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

import dev.adrc95.logronobus.core.domain.DomainConstants.FEED_GAP_BEFORE_TIMETABLE
import dev.adrc95.logronobus.core.domain.DomainConstants.PAST_PASS_GRACE_SECONDS
import dev.adrc95.logronobus.core.model.DepartureTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun isBoardable(
    at: Instant,
    now: Instant,
    windowMinutes: Int,
): Boolean =
    at >= now - PAST_PASS_GRACE_SECONDS.seconds &&
        at <= now + (windowMinutes * 60).seconds

fun unansweredLines(
    internalIds: List<Int>,
    codes: Map<Int, String>,
    answered: Set<String>,
    offered: Set<String>,
): List<Pair<Int, String>> =
    internalIds.mapNotNull { internal ->
        val code = codes[internal] ?: return@mapNotNull null
        if (code in answered || code !in offered) null else internal to code
    }

fun linesBehindFeed(
    internalLines: List<Int>,
    feedAt: Map<Int, Instant>,
    now: Instant,
): List<Int> =
    internalLines.filter { internal ->
        val at = feedAt[internal]
        at == null || at > now + FEED_GAP_BEFORE_TIMETABLE
    }

fun inDirection(
    departures: List<DepartureTime>,
    direction: Int?,
): List<DepartureTime> =
    if (direction == null) {
        departures
    } else {
        departures.filter { it.directionIndex == null || it.directionIndex == direction }
    }
