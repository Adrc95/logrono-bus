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
import dev.adrc95.logronobus.core.network.dto.WalkDestinationDto
import dev.adrc95.logronobus.core.network.dto.WalkRequestDto

class WalkRequestDtoBuilder {
    var origin: LatLngDto = LatLngDtoBuilder().build()
    var destinations: List<WalkDestinationDto> = emptyList()

    fun withOrigin(origin: LatLngDto) = apply { this.origin = origin }

    fun withDestinations(destinations: List<WalkDestinationDto>) = apply { this.destinations = destinations }

    fun build() =
        WalkRequestDto(
            origin = origin,
            destinations = destinations,
        )
}

fun walkRequestDto(block: WalkRequestDtoBuilder.() -> Unit = {}) = WalkRequestDtoBuilder().apply(block).build()
