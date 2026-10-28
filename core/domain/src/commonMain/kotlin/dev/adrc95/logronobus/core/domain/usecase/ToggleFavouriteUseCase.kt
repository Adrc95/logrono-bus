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

import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.model.FavouriteStop
import dev.adrc95.logronobus.core.model.Stop
import org.koin.core.annotation.Factory

@Factory
class ToggleFavouriteUseCase(
    private val favourites: FavouritesRepository,
) {
    suspend operator fun invoke(
        stop: Stop,
        alreadyFavourite: Boolean,
    ) {
        if (alreadyFavourite) {
            favourites.removeFavourite(stop.stopRef)
        } else {
            favourites.addFavourite(
                FavouriteStop(stopRef = stop.stopRef, name = stop.longName, stopId = stop.stopId),
            )
        }
    }
}
