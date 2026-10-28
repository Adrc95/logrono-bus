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

package dev.adrc95.logronobus.core.database

import dev.adrc95.logronobus.core.database.dao.FavouriteStopDao
import dev.adrc95.logronobus.core.database.dao.LearnedDirectionDao
import dev.adrc95.logronobus.core.database.entity.LearnedDirectionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomOnWebIntegrationTest {
    private val favouriteStops: FavouriteStopDao
        get() = TestDatabase.database.favouriteStops()

    private val learnedDirections: LearnedDirectionDao
        get() = TestDatabase.database.learnedDirections()

    @Test
    fun `given a favourite when the worker writes it then the browser storage answers with it`() =
        runTest {
            favouriteStops.clear()
            favouriteStops.insertAtEnd("web-test-stop", "Portales", "web-test-stop-id")

            assertEquals(
                listOf("web-test-stop" to "Portales"),
                favouriteStops.observe().first().map { it.stopRef to it.name },
            )
        }

    @Test
    fun `given learned directions when they are saved then the browser store has them`() =
        runTest {
            learnedDirections.clear()
            learnedDirections.insertAll(
                listOf(LearnedDirectionEntity("9", "direction-9-1", "Las Norias")),
            )

            assertEquals(
                listOf(LearnedDirectionEntity("9", "direction-9-1", "Las Norias")),
                learnedDirections.all(),
            )
        }
}
