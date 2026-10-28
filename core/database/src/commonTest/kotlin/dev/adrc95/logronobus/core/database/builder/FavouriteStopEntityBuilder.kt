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

package dev.adrc95.logronobus.core.database.builder

import dev.adrc95.logronobus.core.database.entity.FavouriteStopEntity

fun favouriteStopEntity(block: FavouriteStopEntityBuilder.() -> Unit = {}): FavouriteStopEntity = FavouriteStopEntityBuilder().apply(block).build()

class FavouriteStopEntityBuilder {
    private var stopRef: String = "360"
    private var name: String = "Plaza de la iglesia El Cortijo"
    private var stopId: String = "360"
    private var position: Int = 0

    fun withStopRef(value: String) = apply { stopRef = value }

    fun withName(value: String) = apply { name = value }

    fun withStopId(value: String) = apply { stopId = value }

    fun withPosition(value: Int) = apply { position = value }

    fun build(): FavouriteStopEntity =
        FavouriteStopEntity(
            stopRef = stopRef,
            name = name,
            stopId = stopId,
            position = position,
        )
}
