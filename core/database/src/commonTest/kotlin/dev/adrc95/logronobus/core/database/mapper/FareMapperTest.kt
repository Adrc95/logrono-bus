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

import dev.adrc95.logronobus.core.database.builder.fareEntity
import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.testing.builder.fare
import kotlin.test.Test
import kotlin.test.assertEquals

class FareMapperTest {
    @Test
    fun given_a_fare_when_it_is_mapped_to_a_row_then_its_enums_are_written_out() {
        val result =
            fare {
                withCode("SINGLE")
                withName("Billete sencillo")
                withPriceCents(130)
                withMunicipality(FareMunicipality.LOGRONO)
                withTraveller(FareTraveller.GENERAL)
                withKind(FareKind.SINGLE)
            }.toEntity()

        assertEquals("SINGLE", result.code)
        assertEquals("Billete sencillo", result.name)
        assertEquals(130, result.priceCents)
        assertEquals("LOGRONO", result.municipality)
        assertEquals("GENERAL", result.traveller)
        assertEquals("SINGLE", result.kind)
    }

    @Test
    fun given_a_stored_fare_row_when_it_is_mapped_back_then_the_enums_come_back() {
        val result =
            fareEntity {
                withCode("MONTH")
                withName("Abono mensual")
                withPriceCents(3500)
                withMunicipality(FareMunicipality.LARDERO)
                withTraveller(FareTraveller.SENIOR)
                withKind(FareKind.MONTH)
            }.toDomain()

        assertEquals("MONTH", result.code)
        assertEquals(3500, result.priceCents)
        assertEquals(FareMunicipality.LARDERO, result.municipality)
        assertEquals(FareTraveller.SENIOR, result.traveller)
        assertEquals(FareKind.MONTH, result.kind)
    }

    @Test
    fun given_a_fare_when_it_goes_to_the_database_and_back_then_it_is_the_same_fare() {
        val original =
            fare {
                withCode("TRIPS_10")
                withName("Bono 10 viajes")
                withPriceCents(850)
                withMunicipality(FareMunicipality.VILLAMEDIANA)
                withTraveller(FareTraveller.STUDENT)
                withKind(FareKind.TRIPS_10)
                withDescription("Bono para estudiantes")
            }

        assertEquals(original, original.toEntity().toDomain())
    }
}
