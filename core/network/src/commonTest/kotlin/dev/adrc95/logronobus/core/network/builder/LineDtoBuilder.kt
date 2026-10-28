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

package dev.adrc95.logronobus.core.network.builder

import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.network.dto.LineStopsDto

class LineDtoBuilder {
    var id: Int = 6
    var name: String = "6-CENTRO-EL CORTIJO"
    var color: String? = "rgba(146,76,72,1)"
    var status: Int = 1
    var stops: LineStopsDto = LineStopsDtoBuilder().build()

    fun withId(id: Int) = apply { this.id = id }

    fun withName(name: String) = apply { this.name = name }

    fun withColor(color: String?) = apply { this.color = color }

    fun withStatus(status: Int) = apply { this.status = status }

    fun withStops(stops: LineStopsDto) = apply { this.stops = stops }

    fun build() =
        LineDto(
            id = id,
            name = name,
            color = color,
            status = status,
            stops = stops,
        )
}

fun lineDto(block: LineDtoBuilder.() -> Unit = {}) = LineDtoBuilder().apply(block).build()
