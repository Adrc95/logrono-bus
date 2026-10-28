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

package dev.adrc95.logronobus

import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals

class StopRouteNameTest {
    private fun stop(ref: String) =
        Stop(
            stopRef = ref,
            stopId = ref,
            longName = "Parada $ref",
            shortName = "Parada $ref",
            latitude = 42.46,
            longitude = -2.44,
            routes = listOf("2"),
        )

    private val names = mapOf("2" to "Yagüe - Varea")
    private val directions = mapOf("2" to listOf(setOf("101", "102"), setOf("201", "202")))

    @Test
    fun `given a stop on the first direction when it is mapped then the line reads as the provider names it`() {
        // When
        val model = stop("101").toDisplayModel(names, directions)

        // Then
        assertEquals(listOf("Yagüe - Varea"), model.routeNames)
    }

    @Test
    fun `given a stop on the way back when it is mapped then the line reads the other way round`() {
        // When
        val model = stop("201").toDisplayModel(names, directions)

        // Then
        assertEquals(
            listOf("Varea - Yagüe"),
            model.routeNames,
            "The bus at that kerb heads for Yagüe, not Varea",
        )
    }
}
