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

package dev.adrc95.logronobus.core.database.mapper

import dev.adrc95.logronobus.core.database.builder.favouriteStopEntity
import dev.adrc95.logronobus.core.testing.builder.favouriteStop
import kotlin.test.Test
import kotlin.test.assertEquals

class FavouriteStopEntityMapperTest {
    @Test
    fun given_a_favourite_stop_entity_when_mapped_to_domain_then_returns_the_favourite_stop() {
        val entity =
            favouriteStopEntity {
                withStopRef("360")
                withName("Plaza de la iglesia El Cortijo")
                withStopId("360")
                withPosition(0)
            }
        val expected =
            favouriteStop {
                withStopRef("360")
                withName("Plaza de la iglesia El Cortijo")
                withStopId("360")
            }

        val result = entity.toDomain()

        assertEquals(expected, result)
    }

    @Test
    fun given_a_favourite_stop_when_mapped_to_entity_then_keeps_the_position() {
        val favourite =
            favouriteStop {
                withStopRef("360")
                withName("Plaza de la iglesia El Cortijo")
                withStopId("360")
            }

        val result = favourite.toEntity(position = 3)

        assertEquals("360", result.stopRef)
        assertEquals("Plaza de la iglesia El Cortijo", result.name)
        assertEquals("360", result.stopId)
        assertEquals(3, result.position)
    }
}
