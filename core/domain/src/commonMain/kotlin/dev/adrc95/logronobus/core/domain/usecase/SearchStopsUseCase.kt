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

package dev.adrc95.logronobus.core.domain.usecase

import dev.adrc95.logronobus.core.domain.DomainConstants
import dev.adrc95.logronobus.core.model.Stop
import org.koin.core.annotation.Factory

@Factory
class SearchStopsUseCase(
    private val getStops: GetStopsUseCase,
) {
    suspend operator fun invoke(query: String): List<Stop> {
        val text = query.trim()
        if (text.length < DomainConstants.MIN_SEARCH_CHARS) return emptyList()
        return getStops()
            .filter { stop -> stop.matches(text) }
            .sortedWith(
                compareBy(
                    { stop -> !stop.starts(text) },
                    { stop -> stop.longName },
                ),
            ).take(DomainConstants.MAX_SEARCH_RESULTS)
    }
}

private fun Stop.matches(text: String): Boolean =
    longName.contains(text, ignoreCase = true) || stopRef.contains(text, ignoreCase = true)

private fun Stop.starts(text: String): Boolean =
    longName.startsWith(text, ignoreCase = true) || stopRef.startsWith(text, ignoreCase = true)
