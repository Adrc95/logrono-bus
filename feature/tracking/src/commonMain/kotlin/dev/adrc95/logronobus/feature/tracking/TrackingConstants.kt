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

package dev.adrc95.logronobus.feature.tracking

object TrackingConstants {
    const val POSITIONS_REFRESH_INTERVAL_MILLIS = 5_000L
    const val BUSES_NOT_LISTED_MILLIS = 8_000L
    const val AT_STOP_METRES = 60.0
    const val AT_STOP_MOVE_METRES = 25.0
}
