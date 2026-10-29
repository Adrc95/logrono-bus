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
import dev.adrc95.logronobus.ui.localizedName
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedNameTest {
    @Test
    fun localizedNameFollowsLocale() {
        val vitoria =
            Stop(
                stopRef = "157",
                stopId = "157",
                longName = "Vitoria",
                shortName = "Vitoria",
                latitude = 42.463968,
                longitude = -2.453118,
            )
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("es"))
            assertEquals("Vitoria", vitoria.localizedName())
            Locale.setDefault(Locale.forLanguageTag("en"))
            assertEquals("Vitoria", vitoria.localizedName())
        } finally {
            Locale.setDefault(original)
        }
    }
}
