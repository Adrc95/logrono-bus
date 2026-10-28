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

package dev.adrc95.logronobus.feature.routes.mapper

import dev.adrc95.logronobus.core.testing.builder.route
import dev.adrc95.logronobus.feature.routes.mapper.toDisplayModel
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteDisplayModelTest {
    @Test
    fun `given a route with the code in its name when mapped then the list shows only the name`() {
        val result =
            route {
                withTimetableId("1")
                withShortName("1")
                withLongName("1-LARDERO-HOSPITAL SAN PEDRO")
            }.toDisplayModel()

        assertEquals("1", result.code)
        assertEquals("LARDERO-HOSPITAL SAN PEDRO", result.name)
    }

    @Test
    fun `given a night route when mapped then the b stays out of the name too`() {
        val result =
            route {
                withTimetableId("B1")
                withShortName("B1")
                withLongName("B1-VAREA-LA CAVA")
            }.toDisplayModel()

        assertEquals("B1", result.code)
        assertEquals("VAREA-LA CAVA", result.name)
    }

    @Test
    fun `given a route without a dash when mapped then the whole name is kept`() {
        val result =
            route {
                withTimetableId("6")
                withShortName("6")
                withLongName("CENTRO")
            }.toDisplayModel()

        assertEquals("CENTRO", result.name)
    }
}
