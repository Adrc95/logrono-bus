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

package dev.adrc95.logronobus.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key
import dev.adrc95.logronobus.core.common.platform.localeChangeRequiresRestart
import dev.adrc95.logronobus.core.model.Stop

expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

fun Stop.localizedName(): String = longName

@Composable
fun ProvideAppLocale(
    languageTag: String?,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppLocale provides languageTag) {
        if (localeChangeRequiresRestart) {
            content()
        } else {
            key(LocalAppLocale.current) {
                content()
            }
        }
    }
}
