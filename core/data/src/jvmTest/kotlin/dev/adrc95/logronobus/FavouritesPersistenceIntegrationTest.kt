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

import dev.adrc95.logronobus.core.testing.TestBus
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import dev.adrc95.logronobus.core.testing.stubbedBackend
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavouritesPersistenceIntegrationTest {
    private val repository = TestBus(injectedHttpClient = stubbedBackend())

    @AfterTest
    fun clearTheStore() =
        runTest {
            repository.favourites.replaceFavouriteStops(emptyList())
        }

    @Test
    fun favouritesRoundTripThroughPersistence() =
        runTest {
            val favourites =
                listOf(
                    favouriteStop {
                        withStopRef("152")
                        withName("Plaza de la iglesia El Cortijo")
                    },
                    favouriteStop {
                        withStopRef("61")
                        withName("Olimpia")
                    },
                )

            repository.favourites.replaceFavouriteStops(favourites)

            assertEquals(
                favourites,
                repository.favourites.observeFavouriteStops().first(),
            )
        }

    @Test
    fun savingEmptyFavouritesClearsPersistedValue() =
        runTest {
            repository.favourites.replaceFavouriteStops(
                listOf(
                    favouriteStop {
                        withStopRef("152")
                        withName("Plaza de la iglesia El Cortijo")
                    },
                ),
            )
            repository.favourites.replaceFavouriteStops(emptyList())

            assertTrue(
                repository.favourites
                    .observeFavouriteStops()
                    .first()
                    .isEmpty(),
            )
        }
}
