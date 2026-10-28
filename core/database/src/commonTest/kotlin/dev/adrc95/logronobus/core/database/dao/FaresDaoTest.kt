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
import dev.adrc95.logronobus.core.database.builder.fareEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FaresDaoTest {
    private lateinit var dao: FaresDao

    @BeforeTest
    fun set_up() {
        dao = TestDatabase.database.fares()
        runTest { dao.clear() }
    }

    @Test
    fun given_an_empty_table_when_reading_then_there_are_no_fares_and_no_timestamp() =
        runTest {
            // When
            val fares = dao.all()
            val storedAt = dao.storedAt()

            // Then
            assertTrue(fares.isEmpty())
            assertNull(storedAt)
        }

    @Test
    fun given_fares_when_inserted_then_they_come_back_with_their_timestamp() =
        runTest {
            // Given
            val single =
                fareEntity {
                    withCode("SINGLE")
                    withStoredAt(1000L)
                }
            val bonus =
                fareEntity {
                    withCode("BONUS")
                    withStoredAt(1000L)
                }

            // When
            dao.insertAll(listOf(single, bonus))

            // Then
            assertEquals(setOf(single, bonus), dao.all().toSet())
            assertEquals(1000L, dao.storedAt())
        }

    @Test
    fun given_a_fare_when_inserted_again_then_it_replaces_the_old_one() =
        runTest {
            // Given
            dao.insertAll(
                listOf(
                    fareEntity {
                        withCode("SINGLE")
                        withName("Old name")
                    },
                ),
            )

            // When
            dao.insertAll(
                listOf(
                    fareEntity {
                        withCode("SINGLE")
                        withName("New name")
                    },
                ),
            )

            // Then
            assertEquals(listOf("New name"), dao.all().map { it.name })
        }

    @Test
    fun given_fares_when_cleared_then_the_table_is_empty_again() =
        runTest {
            // Given
            dao.insertAll(listOf(fareEntity()))

            // When
            dao.clear()

            // Then
            assertTrue(dao.all().isEmpty())
            assertNull(dao.storedAt())
        }
}
