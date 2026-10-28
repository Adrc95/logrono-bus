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

package dev.adrc95.logronobus.core.domain.trip

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DepartureOffersTest {
    @Test
    fun `given a code with a b in front when it is checked then it is a night line`() {
        assertTrue(isNightLine("B1"))
        assertTrue(isNightLine("B3"))
        assertFalse(isNightLine("1"))
    }

    @Test
    fun `given a day line when it is offered then it always is`() {
        assertTrue(
            offersTonight(
                code = "6",
                nightLines = setOf("B1", "B3"),
                runningTonight = emptySet(),
            ),
        )
    }

    @Test
    fun `given a night line when the fleet has it out then it is offered`() {
        assertFalse(
            offersTonight(
                code = "B1",
                nightLines = setOf("B1"),
                runningTonight = emptySet(),
            ),
            "without the bus out the timetable would promise a bus that is not running",
        )
        assertTrue(
            offersTonight(
                code = "B1",
                nightLines = setOf("B1"),
                runningTonight = setOf("B1"),
            ),
        )
    }
}
