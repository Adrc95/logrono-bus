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

package dev.adrc95.logronobus.feature.fares.mapper

import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.network.dto.FareProductDto
import dev.adrc95.logronobus.core.network.mapper.toDomain
import dev.adrc95.logronobus.feature.fares.mapper.toDisplayModel
import dev.adrc95.logronobus.feature.fares.mapper.toDisplayModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FareDisplayMapperTest {
    private fun product(
        name: String,
        price: Int,
        code: String = "1",
        description: String? = null,
        maxQuantity: Int = 60,
    ) = FareProductDto(
        productCode = code,
        name = name,
        price = price,
        description = description,
        maxQuantity = maxQuantity,
    )

    @Test
    fun `given a fare title when it is mapped then the municipality is the one it is sold for`() {
        assertEquals(FareMunicipality.LOGRONO, product("Bonobus", 529).toDomain().municipality)
        assertEquals(
            FareMunicipality.LARDERO,
            product("Bonobus Lardero", 529).toDomain().municipality,
        )
        assertEquals(
            FareMunicipality.VILLAMEDIANA,
            product("Bonobus Villamediana", 529).toDomain().municipality,
        )
    }

    @Test
    fun `given a fare title when it is mapped then the traveller is read off the name most specific first`() {
        assertEquals(FareTraveller.GENERAL, product("Bonobus", 529).toDomain().traveller)
        assertEquals(FareTraveller.STUDENT, product("Estudiante FN20", 430).toDomain().traveller)
        assertEquals(FareTraveller.SENIOR, product("Jubilado FN50", 125).toDomain().traveller)
        assertEquals(
            FareTraveller.LARGE_FAMILY_50,
            product("Bonobus FN50", 265).toDomain().traveller,
        )
        assertEquals(
            FareTraveller.LARGE_FAMILY_20,
            product("Bonobus FN20", 423).toDomain().traveller,
        )
        assertEquals(FareTraveller.CHILD, product("Sencillo Niño", 0).toDomain().traveller)
        assertEquals(FareTraveller.CHILD, product("Bonopeque", 0).toDomain().traveller)
        assertEquals(FareTraveller.REDUCED, product("Tarifa reducida", 163).toDomain().traveller)
    }

    @Test
    fun `given a fare in the operator's cents when it is mapped then the price is shown as euros`() {
        assertEquals("5,29 €", product("Bonobus", 529).toDomain().toDisplayModel().priceLabel)
        assertEquals("1,00 €", product("Sencillo", 100).toDomain().toDisplayModel().priceLabel)
        assertEquals("25,00 €", product("Bonomes", 2500).toDomain().toDisplayModel().priceLabel)
        assertEquals("0,50 €", product("Medio", 50).toDomain().toDisplayModel().priceLabel)
        assertEquals("0,00 €", product("Bonopeque", 0).toDomain().toDisplayModel().priceLabel)
    }

    @Test
    fun `given a fare title when it is mapped then what it buys comes from the name`() {
        assertEquals(FareKind.TRIPS_10, product("Bonobus", 529).toDomain().kind)
        assertEquals(FareKind.TRIPS_10, product("10 viajes QR", 529).toDomain().kind)
        assertEquals(FareKind.MONTH, product("Bonomes", 2500).toDomain().kind)
        assertEquals(FareKind.SINGLE, product("Sencillo", 100).toDomain().kind)
        assertEquals(FareKind.OTHER, product("Búho", 100).toDomain().kind)
    }

    @Test
    fun `given a municipality and a traveller when the catalogue is filtered then only matching fares remain`() {
        val fares =
            listOf(
                product("Bonobus", 529).toDomain(),
                product("Bonobus Lardero", 529).toDomain(),
                product("Jubilado FN50", 125).toDomain(),
                product("Estudiante", 538).toDomain(),
            )
        assertEquals(3, fares.toDisplayModels(FareMunicipality.LOGRONO, FareTraveller.ALL).size)
        assertEquals(
            listOf("Jubilado FN50"),
            fares.toDisplayModels(FareMunicipality.LOGRONO, FareTraveller.SENIOR).map { it.name },
        )
        assertEquals(2, fares.toDisplayModels(null, FareTraveller.GENERAL).size)
        assertTrue(
            fares.toDisplayModels(FareMunicipality.LARDERO, FareTraveller.SENIOR).isEmpty(),
            "no intersection, no invented row",
        )
    }

    @Test
    fun `given a catalogue when it is mapped then the cheapest fare comes first`() {
        val sorted =
            listOf(
                product("Bonomes", 2500).toDomain(),
                product("Sencillo", 100).toDomain(),
                product("Bonobus", 529).toDomain(),
            ).toDisplayModels(null, FareTraveller.ALL)
        assertEquals(listOf("Sencillo", "Bonobus", "Bonomes"), sorted.map { it.name })
    }

    @Test
    fun `given a fare with a description when it is mapped then the description is untouched`() {
        val fare = product("Bonomes", 2500, description = "Renovable por 30 días.").toDomain()
        assertEquals("Renovable por 30 días.", fare.description)
    }
}
