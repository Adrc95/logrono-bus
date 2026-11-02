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

package dev.adrc95.logronobus.feature.routes

import dev.adrc95.logronobus.core.navigation.deeplink.DeepLinks

object RoutesConstants {
    const val TRACKING_REFRESH_INTERVAL_MILLIS = 10_000L
    const val MAX_NEXT_STOP_LEAD_MINUTES = 30L
    const val MAX_TERMINUS_WAIT_MINUTES = 60L
    const val LINE_PARAM: String = "line"
    const val DEEP_LINK: String = "${DeepLinks.BASE_URI}routes?$LINE_PARAM={$LINE_PARAM}"
}
