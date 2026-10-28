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

import dev.adrc95.logronobus.core.model.LinePasses

class LinePassesBuilder {
    var overall: List<String> = emptyList()
    var byDirection: Map<String, List<String>> = emptyMap()

    fun withOverall(overall: List<String>) = apply { this.overall = overall }

    fun withByDirection(byDirection: Map<String, List<String>>) = apply { this.byDirection = byDirection }

    fun build() =
        LinePasses(
            overall = overall,
            byDirection = byDirection,
        )
}

fun linePasses(block: LinePassesBuilder.() -> Unit = {}) = LinePassesBuilder().apply(block).build()
