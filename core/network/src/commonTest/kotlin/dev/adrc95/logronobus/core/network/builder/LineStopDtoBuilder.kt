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

import dev.adrc95.logronobus.core.network.dto.LatLngDto
import dev.adrc95.logronobus.core.network.dto.LineStopDto

class LineStopDtoBuilder {
    var id: Int = 0
    var name: String = "sample"
    var lat: Double = 0.0
    var lng: Double = 0.0
    var status: Int = 0
    var onwardShape: List<LatLngDto> = emptyList()

    fun withId(id: Int) = apply { this.id = id }

    fun withName(name: String) = apply { this.name = name }

    fun withLat(lat: Double) = apply { this.lat = lat }

    fun withLng(lng: Double) = apply { this.lng = lng }

    fun withStatus(status: Int) = apply { this.status = status }

    fun withOnwardShape(onwardShape: List<LatLngDto>) = apply { this.onwardShape = onwardShape }

    fun build() =
        LineStopDto(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            status = status,
            onwardShape = onwardShape,
        )
}

fun lineStopDto(block: LineStopDtoBuilder.() -> Unit = {}) = LineStopDtoBuilder().apply(block).build()
