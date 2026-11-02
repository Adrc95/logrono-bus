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

package dev.adrc95.logronobus.feature.stops

import dev.adrc95.logronobus.core.navigation.deeplink.DeepLinks

object StopsConstants {
    const val STOP_LOOKUP_RETRY_MILLIS = 500L
    private const val LINE_PARAM: String = "line"
    private const val DIRECTION_PARAM: String = "direction"
    const val DEEP_LINK: String =
        "${DeepLinks.BASE_URI}stops/{stopRef}" +
            "?${LINE_PARAM}={lineFilter}" +
            "&${DIRECTION_PARAM}={directionFilter}"
}
