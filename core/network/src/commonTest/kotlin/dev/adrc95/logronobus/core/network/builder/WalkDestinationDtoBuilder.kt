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

import dev.adrc95.logronobus.core.network.dto.WalkDestinationDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class WalkDestinationDtoBuilder {
    var id: JsonElement = JsonPrimitive("360")
    var lat: Double = 42.46
    var lng: Double = -2.44

    fun withId(id: JsonElement) = apply { this.id = id }

    fun withLat(lat: Double) = apply { this.lat = lat }

    fun withLng(lng: Double) = apply { this.lng = lng }

    fun build() =
        WalkDestinationDto(
            id = id,
            lat = lat,
            lng = lng,
        )
}

fun walkDestinationDto(block: WalkDestinationDtoBuilder.() -> Unit = {}) = WalkDestinationDtoBuilder().apply(block).build()
