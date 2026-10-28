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

import dev.adrc95.logronobus.core.ui.util.routeName
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteNameTest {
    @Test
    fun `given a catalogue name with its code when it is shown then the code goes away and every part is title cased`() {
        // Given
        val longName = "2-YAGÜE-VAREA"

        // When
        val name = longName.routeName()

        // Then
        assertEquals("Yagüe - Varea", name)
    }

    @Test
    fun `given a catalogue name with connectors when it is shown then only the connectors stay lowercase`() {
        // Given
        val longName = "4-PALACIO DE CONGRESOS-PRADOVIEJO"

        // When
        val name = longName.routeName()

        // Then
        assertEquals("Palacio de Congresos - Pradoviejo", name)
    }

    @Test
    fun `given a name without code when it is shown then it is title cased as it is`() {
        // Given
        val longName = "EL CORTIJO"

        // When
        val name = longName.routeName()

        // Then
        assertEquals("El Cortijo", name)
    }
}
