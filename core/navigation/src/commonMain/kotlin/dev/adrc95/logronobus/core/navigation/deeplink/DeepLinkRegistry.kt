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
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import kotlinx.serialization.KSerializer

class DeepLinkRegistry {
    private val matchers = mutableMapOf<String, UriDeepLinkMatcher<*>>()

    fun <T : NavKey> register(
        pattern: String,
        serializer: KSerializer<T>,
    ) {
        matchers[pattern] = UriDeepLinkMatcher(DeepLinkUri(pattern), serializer)
    }

    fun resolve(uri: String): DeepLinkMatch? {
        if (uri.isBlank()) return null
        val request = DeepLinkRequest(uri)
        // A screen can answer to several patterns, so the most specific match wins.
        val match =
            matchers.values
                .mapNotNull { matcher -> matcher.match(request) }
                .maxWithOrNull { first, second -> first.compareTo(second) }
                ?: return null
        val key = match.key as? NavKey ?: return null
        return DeepLinkMatch(key, match.arguments)
    }
}
