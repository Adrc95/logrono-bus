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
import dev.adrc95.logronobus.core.database.builder.learnedDirectionEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LearnedDirectionDaoTest {
    private val dao: LearnedDirectionDao
        get() = TestDatabase.database.learnedDirections()

    private suspend fun reset() {
        dao.clear()
    }

    @Test
    fun given_no_learned_directions_when_reading_then_the_list_is_empty() =
        runTest {
            reset()
            assertTrue(dao.all().isEmpty())
        }

    @Test
    fun given_a_learned_direction_when_inserted_then_it_can_be_read() =
        runTest {
            reset()
            val direction =
                learnedDirectionEntity {
                    withLineCode("9")
                    withDirectionRef("direction-9-1")
                    withHeadsign("Las Norias")
                }

            dao.insertAll(listOf(direction))

            assertEquals(listOf(direction), dao.all())
        }

    @Test
    fun given_a_learned_direction_when_inserted_again_then_it_is_replaced() =
        runTest {
            reset()
            dao.insertAll(
                listOf(
                    learnedDirectionEntity {
                        withLineCode("9")
                        withDirectionRef("direction-9-1")
                        withHeadsign("Old headsign")
                    },
                ),
            )

            val replacement =
                learnedDirectionEntity {
                    withLineCode("9")
                    withDirectionRef("direction-9-1")
                    withHeadsign("New headsign")
                }
            dao.insertAll(listOf(replacement))

            assertEquals(listOf(replacement), dao.all())
        }

    @Test
    fun given_learned_directions_when_cleared_then_the_table_is_empty() =
        runTest {
            reset()
            dao.insertAll(
                listOf(
                    learnedDirectionEntity {
                        withLineCode("9")
                        withDirectionRef("direction-9-1")
                        withHeadsign("Las Norias")
                    },
                ),
            )

            dao.clear()

            assertTrue(dao.all().isEmpty())
        }
}
