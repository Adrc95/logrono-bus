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
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

actual object LocalAppLocale {
    private val localAppLocale = staticCompositionLocalOf<String?> { null }

    actual val current: String
        @Composable get() = localAppLocale.current ?: "en"

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (value != null) applyBrowserLocale(value)
        return localAppLocale.provides(value)
    }
}

// Compose reads the language it serves resources in from `navigator.languages`, and reloads that
// list when the window fires `languagechange`, so overriding both is what makes the switch work.
@OptIn(ExperimentalWasmJsInterop::class)
private val applyBrowserLocale: (String) -> Unit =
    js(
        """(tag) => {
            Object.defineProperty(window.navigator, 'languages', {
                get: () => [tag],
                configurable: true,
            });
            Object.defineProperty(window.navigator, 'language', {
                get: () => tag,
                configurable: true,
            });
            window.dispatchEvent(new Event('languagechange'));
        }""",
    )
