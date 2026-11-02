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

package dev.adrc95.logronobus.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
private data object ScreenKey : NavKey

@Serializable
private data class StopKey(
    val stopRef: String,
    val line: String? = null,
    val direction: Int? = null,
) : NavKey

class DeepLinkRegistryTest {
    private val deepLinks =
        DeepLinkRegistry().apply {
            register("logronobus://screen", ScreenKey.serializer())
            register("logronobus://stops/{stopRef}?line={line}&direction={direction}", StopKey.serializer())
            register("logronobus://screen?line={line}", ScreenKey.serializer())
        }

    @Test
    fun `given a screen link when it is resolved then its screen is returned`() {
        assertEquals(ScreenKey, deepLinks.resolve("logronobus://screen")?.key)
    }

    @Test
    fun `given a link with arguments when it is resolved then they are handed over`() {
        assertEquals(
            mapOf("line" to listOf("6")),
            deepLinks.resolve("logronobus://screen?line=6")?.arguments,
        )
    }

    @Test
    fun `given a link with a path and a query when it is resolved then the arguments are decoded`() {
        assertEquals(
            StopKey(stopRef = "123", line = "6", direction = 1),
            deepLinks.resolve("logronobus://stops/123?line=6&direction=1")?.key,
        )
    }

    @Test
    fun `given a link without the optional arguments when it is resolved then the defaults are kept`() {
        assertEquals(StopKey(stopRef = "123"), deepLinks.resolve("logronobus://stops/123")?.key)
    }

    @Test
    fun `given a link with arguments nobody declared when it is resolved then they are ignored`() {
        assertEquals(ScreenKey, deepLinks.resolve("logronobus://screen?utm_source=qr")?.key)
    }

    @Test
    fun `given a link nobody registered when it is resolved then nothing is returned`() {
        assertNull(deepLinks.resolve("logronobus://unknown"))
    }

    @Test
    fun `given a link missing a required argument when it is resolved then nothing is returned`() {
        assertNull(deepLinks.resolve("logronobus://stops"))
    }

    @Test
    fun `given the same link registered twice when it is resolved then it is still resolved`() {
        val registry = DeepLinkRegistry()
        registry.register("logronobus://screen", ScreenKey.serializer())
        registry.register("logronobus://screen", ScreenKey.serializer())

        assertEquals(ScreenKey, registry.resolve("logronobus://screen")?.key)
    }

    @Test
    fun `given a blank link when it is resolved then nothing is returned`() {
        assertNull(deepLinks.resolve(""))
    }
}
