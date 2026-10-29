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
import dev.adrc95.logronobus.core.database.builder.catalogueLineEntity
import dev.adrc95.logronobus.core.database.builder.catalogueStopEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogueDaoTest {
    private val dao: CatalogueDao
        get() = TestDatabase.database.catalogue()

    private suspend fun reset() {
        dao.clearStops()
        dao.clearLines()
    }

    @Test
    fun given_an_empty_catalogue_when_reading_then_there_are_no_rows_and_no_timestamps() =
        runTest {
            reset()
            // When
            val stops = dao.allStops()
            val lines = dao.allLines()

            // Then
            assertTrue(stops.isEmpty())
            assertTrue(lines.isEmpty())
            assertNull(dao.stopsStoredAt())
            assertNull(dao.linesStoredAt())
        }

    @Test
    fun given_stops_when_inserted_then_they_come_back_with_their_timestamp() =
        runTest {
            reset()
            // Given
            val first =
                catalogueStopEntity {
                    withId(5)
                    withName("Artesanos")
                    withStoredAt(2000L)
                }
            val second =
                catalogueStopEntity {
                    withId(130)
                    withName("La Cadena")
                    withStoredAt(2000L)
                }

            // When
            dao.insertStops(listOf(first, second))

            // Then
            assertEquals(setOf(first, second), dao.allStops().toSet())
            assertEquals(2000L, dao.stopsStoredAt())
        }

    @Test
    fun given_lines_when_inserted_then_they_come_back_with_their_code() =
        runTest {
            reset()
            // Given
            val night =
                catalogueLineEntity {
                    withCode("B1")
                    withId(31)
                    withName("B1-VAREA-LA CAVA")
                    withStoredAt(2000L)
                }
            val day =
                catalogueLineEntity {
                    withCode("6")
                    withId(6)
                    withStoredAt(2000L)
                }

            // When
            dao.insertLines(listOf(night, day))

            // Then
            assertEquals(setOf("B1", "6"), dao.allLines().map { it.code }.toSet())
            assertEquals(2000L, dao.linesStoredAt())
        }

    @Test
    fun given_stops_when_cleared_then_the_table_is_empty_again() =
        runTest {
            reset()
            // Given
            dao.insertStops(listOf(catalogueStopEntity()))

            // When
            dao.clearStops()

            // Then
            assertTrue(dao.allStops().isEmpty())
            assertNull(dao.stopsStoredAt())
        }
}
