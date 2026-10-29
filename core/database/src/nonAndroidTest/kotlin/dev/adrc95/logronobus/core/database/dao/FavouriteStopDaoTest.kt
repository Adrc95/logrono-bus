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

package dev.adrc95.logronobus.core.database.dao

import dev.adrc95.logronobus.core.database.TestDatabase
import dev.adrc95.logronobus.core.database.builder.favouriteStopEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavouriteStopDaoTest {
    private val dao: FavouriteStopDao
        get() = TestDatabase.database.favouriteStops()

    private suspend fun reset() {
        dao.clear()
    }

    @Test
    fun given_no_favourites_when_reading_then_the_list_is_empty() =
        runTest {
            reset()
            assertTrue(dao.observe().first().isEmpty())
        }

    @Test
    fun given_a_favourite_when_upserted_then_it_can_be_read() =
        runTest {
            reset()
            val entity =
                favouriteStopEntity {
                    withStopRef("360")
                    withName("Plaza de la iglesia El Cortijo")
                    withStopId("360")
                    withPosition(0)
                }

            dao.upsert(entity)

            assertEquals(listOf(entity), dao.observe().first())
        }

    @Test
    fun given_a_favourite_when_upserted_again_then_it_is_replaced() =
        runTest {
            reset()
            dao.upsert(
                favouriteStopEntity {
                    withStopRef("360")
                    withName("Old name")
                },
            )

            val replacement =
                favouriteStopEntity {
                    withStopRef("360")
                    withName("New name")
                }
            dao.upsert(replacement)

            assertEquals(listOf(replacement), dao.observe().first())
        }

    @Test
    fun given_several_favourites_when_reading_then_they_come_back_in_position_order() =
        runTest {
            reset()
            val first =
                favouriteStopEntity {
                    withStopRef("360")
                    withName("El Cortijo")
                    withPosition(0)
                }
            val second =
                favouriteStopEntity {
                    withStopRef("61")
                    withName("Olimpia")
                    withPosition(1)
                }

            dao.upsertAll(listOf(second, first))

            assertEquals(listOf(first, second), dao.observe().first())
        }

    @Test
    fun given_a_favourite_when_deleted_then_it_is_gone() =
        runTest {
            reset()
            dao.upsert(favouriteStopEntity { withStopRef("360") })
            dao.upsert(favouriteStopEntity { withStopRef("61") })

            dao.delete("360")

            assertEquals(listOf("61"), dao.observe().first().map { it.stopRef })
        }

    @Test
    fun given_favourites_when_cleared_then_the_table_is_empty() =
        runTest {
            reset()
            dao.upsert(favouriteStopEntity { withStopRef("360") })

            dao.clear()

            assertTrue(dao.observe().first().isEmpty())
        }

    @Test
    fun given_favourites_when_inserted_at_the_end_then_they_keep_the_order_they_were_added() =
        runTest {
            reset()
            dao.upsert(
                favouriteStopEntity {
                    withStopRef("360")
                    withPosition(0)
                },
            )

            dao.insertAtEnd("61", "Olimpia", "61")
            dao.insertAtEnd("999", "Gran Via", "999")

            assertEquals(
                listOf("360", "61", "999"),
                dao.observe().first().map { it.stopRef },
            )
        }
}
