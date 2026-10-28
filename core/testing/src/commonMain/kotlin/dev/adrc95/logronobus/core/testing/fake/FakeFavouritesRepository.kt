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

package dev.adrc95.logronobus.core.testing.fake

import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.model.FavouriteStop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeFavouritesRepository(
    initial: List<FavouriteStop> = emptyList(),
) : FavouritesRepository {
    private val stops = MutableStateFlow(initial)

    override fun observeFavouriteStops(): Flow<List<FavouriteStop>> = stops

    override suspend fun addFavourite(stop: FavouriteStop) {
        stops.update { current -> current.filterNot { it.stopRef == stop.stopRef } + stop }
    }

    override suspend fun removeFavourite(stopRef: String) {
        stops.update { current -> current.filterNot { it.stopRef == stopRef } }
    }

    override suspend fun replaceFavouriteStops(favourites: List<FavouriteStop>) {
        stops.value = favourites
    }
}
