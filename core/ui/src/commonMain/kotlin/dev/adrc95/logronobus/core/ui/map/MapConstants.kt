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

package dev.adrc95.logronobus.core.ui.map

const val LOGRONO_LAT = 42.4655
const val LOGRONO_LON = -2.4443
const val DEFAULT_ZOOM = 14
const val MIN_ZOOM = 10
const val MAX_ZOOM = 18
const val TILE_PX = 256
const val STOP_MARKER_MIN_ZOOM = 13
const val MIN_MOVE_METRES = 12.0
const val JITTER_DEADBAND_METRES = 6.0
const val GLIDE_MILLIS = 4000L
const val FOLLOW_DEADBAND_METRES = 25.0
const val FOLLOW_ZOOM = 16.5
const val FOLLOW_SETTLE_MILLIS = 400L
const val TILE_CACHE_MAX = 320
const val TILE_FETCH_CONCURRENCY = 6
const val TILE_PREFETCH_RING = 1
const val TILE_TIMEOUT_MILLIS = 10_000
const val TILE_USER_AGENT = "LogronoBusApp/1.0"
const val STOP_ICON = "bus_11"
const val STYLE_LIGHT = "https://tiles.openfreemap.org/styles/positron"
const val STYLE_DARK = "https://tiles.openfreemap.org/styles/fiord"
const val STOP_TAP_METRES = 60.0
const val BUS_TAP_METRES = 80.0
const val MAX_FIT_STOPS = 40
