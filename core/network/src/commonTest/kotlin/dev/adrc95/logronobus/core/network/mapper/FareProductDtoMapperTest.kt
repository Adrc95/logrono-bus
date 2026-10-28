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

package dev.adrc95.logronobus.core.network.mapper

import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.network.builder.fareProductDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FareProductDtoMapperTest {
    @Test
    fun given_a_fare_product_when_mapped_to_domain_then_it_carries_the_price_and_the_code() {
        val dto =
            fareProductDto {
                withProductCode("1")
                withName("Billete sencillo")
                withPrice(135)
                withDescription("Un viaje")
                withMaxQuantity(60)
            }

        val result = dto.toDomain()

        assertEquals("1", result.code)
        assertEquals("Billete sencillo", result.name)
        assertEquals(135, result.priceCents)
        assertEquals("Un viaje", result.description)
    }

    @Test
    fun given_a_fare_product_without_a_price_when_mapped_then_the_price_is_zero() {
        val dto =
            fareProductDto {
                withPrice(null)
            }

        assertEquals(0, dto.toDomain().priceCents)
    }

    @Test
    fun given_a_fare_product_of_another_municipality_when_mapped_then_the_municipality_is_that_one() {
        val dto =
            fareProductDto {
                withName("Abono Lardero")
            }

        assertEquals(FareMunicipality.LARDERO, dto.toDomain().municipality)
    }

    @Test
    fun given_a_fare_product_for_students_when_mapped_then_the_traveller_is_the_student_one() {
        val dto =
            fareProductDto {
                withName("Estudiante")
            }

        assertEquals(FareTraveller.STUDENT, dto.toDomain().traveller)
    }

    @Test
    fun given_a_fare_product_without_a_description_when_mapped_then_the_description_is_null() {
        val dto =
            fareProductDto {
                withDescription(null)
            }

        assertNull(dto.toDomain().description)
    }

    @Test
    fun given_a_fare_product_when_mapped_then_the_kind_is_a_single_ticket_by_default() {
        val dto =
            fareProductDto {
                withName("Billete sencillo")
            }

        assertEquals(FareKind.SINGLE, dto.toDomain().kind)
    }
}
