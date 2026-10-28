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

package dev.adrc95.logronobus.core.domain

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object DomainConstants {
    const val DEFAULT_WINDOW_MINUTES = 90
    const val TERMINUS_ARRIVAL_METRES = 60.0
    const val TERMINUS_OVERSHOOT_METRES = 250.0
    const val MAX_BUS_METRES_PER_SECOND = 20.0
    const val OFF_ROUTE_METRES = 200.0
    const val STREET_DETOUR = 1.3
    const val METRES_PER_MINUTE = 200.0
    const val AT_STOP_METRES = 35.0
    const val AT_STOP_MOVE_METRES = 25.0
    const val THEORETICAL_ETA_ORDER_TOLERANCE = 2
    const val BUS_INDEX_SETTLE_METRES = 20.0
    const val BUS_INDEX_BACKTRACK_METRES = 150.0
    const val MIN_MOVEMENT_METRES = 100.0
    const val SAME_TRIP_TOLERANCE_SECONDS = 300L
    const val PAST_PASS_GRACE_SECONDS = 60L
    const val TRIP_MATCH_TOLERANCE_SECONDS = 300L
    val FEED_GAP_BEFORE_TIMETABLE = 5.minutes
    val STALE_PREDICTION_GRACE = 45.seconds
    const val MIN_STOPS_OF_THE_SIGN = 6
    const val FRAMES_WITHOUT_STOP = 5
    const val MIN_SEARCH_CHARS = 2
    const val MAX_SEARCH_RESULTS = 30
    const val SCAN_MIN_FIT = 3
    const val SCAN_MISREAD_PERCENT = 25
    const val SCAN_MIN_SHARED_PERCENT = 60
    const val SCAN_LONGER_SIGN_PERCENT = 14
    const val SCAN_NIGHT_LINE_NAME = "BÜHO"
    const val SCAN_MAX_LINE_CANDIDATES = 3
    const val SCAN_STACKED_GAP = 0.6f
    const val SCAN_ALIGNMENT = 3f
    const val SCAN_DRIFT = 4f
    const val SCAN_GAP = 6f
    const val SCAN_MIN_STOPS = 5
    const val SCAN_SAME_ROW_SIZE = 0.5f
    const val SCAN_SAME_ROW_GAP = 1.2f
    const val SCAN_SAME_ROW_SHARED = 0.6f
    const val SCAN_MAJOR_NAME = 0.6f
    const val SCAN_MAX_DIRECTIONS = 4
    const val SCAN_MARKED_SHARE = 10
    const val SCAN_MARKED_COLOUR = 40
    const val SCAN_MARKED_DOT = 60
    const val SCAN_MARKED_SPREAD = 15
    const val SCAN_MARKED_LEAD = 10
    const val SCAN_MARKED_BAND = 15
    const val SCAN_MIN_READ_STOPS = 2
    const val MEANINGFULLY_STALE_SECONDS = 90L
    val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    val WHITESPACE_REGEX = Regex("\\s+")
}
