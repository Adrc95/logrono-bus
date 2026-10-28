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

import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.next_stop
import dev.adrc95.logronobus.core.ui.generated.resources.your_stop
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class StringResourcesTest {
    @Test
    fun stringResourcesResolvePerLocale() =
        runTest {
            val original = Locale.getDefault()
            try {
                Locale.setDefault(Locale.forLanguageTag("es"))
                assertEquals("Próxima parada", getString(Res.string.next_stop))
                assertEquals("Tu parada", getString(Res.string.your_stop))

                Locale.setDefault(Locale.forLanguageTag("en"))
                assertEquals("Next stop", getString(Res.string.next_stop))
                assertEquals("Your stop", getString(Res.string.your_stop))
            } finally {
                Locale.setDefault(original)
            }
        }
}
