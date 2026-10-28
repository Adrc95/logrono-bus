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

import dev.adrc95.logronobus.core.network.dto.WalkItemDto
import kotlinx.serialization.json.JsonElement

class WalkItemDtoBuilder {
    var id: JsonElement? = null
    var distanceM: Int? = null
    var durationSec: Int? = null

    fun withId(id: JsonElement?) = apply { this.id = id }

    fun withDistanceM(distanceM: Int?) = apply { this.distanceM = distanceM }

    fun withDurationSec(durationSec: Int?) = apply { this.durationSec = durationSec }

    fun build() =
        WalkItemDto(
            id = id,
            distanceM = distanceM,
            durationSec = durationSec,
        )
}

fun walkItemDto(block: WalkItemDtoBuilder.() -> Unit = {}) = WalkItemDtoBuilder().apply(block).build()
