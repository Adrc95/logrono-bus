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

package dev.adrc95.logronobus.core.testing.builder

import dev.adrc95.logronobus.core.model.Route

fun route(block: RouteBuilder.() -> Unit = {}): Route = RouteBuilder().apply(block).build()

class RouteBuilder {
    private var timetableId: String = "6"
    private var longName: String = "6-CENTRO-EL CORTIJO"
    private var shortName: String = "6"
    private var color: String? = "rgba(146,76,72,1)"

    fun withTimetableId(value: String) = apply { timetableId = value }

    fun withLongName(value: String) = apply { longName = value }

    fun withShortName(value: String) = apply { shortName = value }

    fun withColor(value: String?) = apply { color = value }

    fun build(): Route =
        Route(
            timetableId = timetableId,
            longName = longName,
            shortName = shortName,
            color = color,
        )
}
