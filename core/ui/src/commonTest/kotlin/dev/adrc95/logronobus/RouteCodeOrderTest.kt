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

import dev.adrc95.logronobus.core.ui.util.routeCodeComparator
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteCodeOrderTest {
    @Test
    fun `given plain and prefixed line codes when they are ordered then the plain numbers come first and the night lines after`() {
        val codes = listOf("B2", "10", "B1", "2", "33")
        assertEquals(
            listOf("2", "10", "33", "B1", "B2"),
            codes.sortedWith(routeCodeComparator()),
        )
    }
}
