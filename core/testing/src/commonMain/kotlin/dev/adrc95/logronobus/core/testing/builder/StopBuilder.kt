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

import dev.adrc95.logronobus.core.model.Stop

fun stop(block: StopBuilder.() -> Unit = {}): Stop = StopBuilder().apply(block).build()

class StopBuilder {
    private var stopRef: String = "360"
    private var stopId: String = "360"
    private var longName: String = "Plaza de la iglesia El Cortijo"
    private var shortName: String = "Plaza de la iglesia El Cortijo"
    private var latitude: Double = 42.492503
    private var longitude: Double = -2.520566
    private var routes: List<String>? = listOf("6")
    private var direction: String? = null

    fun withStopRef(value: String) = apply { stopRef = value }

    fun withStopId(value: String) = apply { stopId = value }

    fun withLongName(value: String) = apply { longName = value }

    fun withShortName(value: String) = apply { shortName = value }

    fun withLatitude(value: Double) = apply { latitude = value }

    fun withLongitude(value: Double) = apply { longitude = value }

    fun withRoutes(value: List<String>?) = apply { routes = value }

    fun withDirection(value: String?) = apply { direction = value }

    fun build(): Stop =
        Stop(
            stopRef = stopRef,
            stopId = stopId,
            longName = longName,
            shortName = shortName,
            latitude = latitude,
            longitude = longitude,
            routes = routes,
            direction = direction,
        )
}
