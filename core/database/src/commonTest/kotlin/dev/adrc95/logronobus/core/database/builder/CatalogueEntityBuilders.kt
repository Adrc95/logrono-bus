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

package dev.adrc95.logronobus.core.database.builder

import dev.adrc95.logronobus.core.database.entity.CatalogueLineEntity
import dev.adrc95.logronobus.core.database.entity.CatalogueStopEntity
import dev.adrc95.logronobus.core.database.entity.FareEntity
import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller

fun catalogueStopEntity(block: CatalogueStopEntityBuilder.() -> Unit = {}): CatalogueStopEntity = CatalogueStopEntityBuilder().apply(block).build()

class CatalogueStopEntityBuilder {
    private var id: Int = 360
    private var name: String = "Plaza de la iglesia El Cortijo"
    private var lat: Double = 42.492503
    private var lng: Double = -2.520566
    private var status: Int = 1
    private var storedAt: Long = 0

    fun withId(value: Int) = apply { id = value }

    fun withName(value: String) = apply { name = value }

    fun withLat(value: Double) = apply { lat = value }

    fun withLng(value: Double) = apply { lng = value }

    fun withStatus(value: Int) = apply { status = value }

    fun withStoredAt(value: Long) = apply { storedAt = value }

    fun build(): CatalogueStopEntity =
        CatalogueStopEntity(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            status = status,
            storedAt = storedAt,
        )
}

fun catalogueLineEntity(block: CatalogueLineEntityBuilder.() -> Unit = {}): CatalogueLineEntity = CatalogueLineEntityBuilder().apply(block).build()

class CatalogueLineEntityBuilder {
    private var id: Int = 6
    private var code: String = "6"
    private var name: String = "6-CENTRO-EL CORTIJO"
    private var colour: String? = "rgba(146,76,72,1)"
    private var status: Int = 1
    private var storedAt: Long = 0

    fun withId(value: Int) = apply { id = value }

    fun withCode(value: String) = apply { code = value }

    fun withName(value: String) = apply { name = value }

    fun withColour(value: String?) = apply { colour = value }

    fun withStatus(value: Int) = apply { status = value }

    fun withStoredAt(value: Long) = apply { storedAt = value }

    fun build(): CatalogueLineEntity =
        CatalogueLineEntity(
            code = code,
            id = id,
            name = name,
            colour = colour,
            status = status,
            storedAt = storedAt,
        )
}

fun fareEntity(block: FareEntityBuilder.() -> Unit = {}): FareEntity = FareEntityBuilder().apply(block).build()

class FareEntityBuilder {
    private var code: String = "SINGLE"
    private var name: String = "Billete sencillo"
    private var priceCents: Int = 130
    private var municipality: String = FareMunicipality.LOGRONO.name
    private var traveller: String = FareTraveller.GENERAL.name
    private var kind: String = FareKind.SINGLE.name
    private var description: String? = null
    private var storedAt: Long = 0

    fun withCode(value: String) = apply { code = value }

    fun withName(value: String) = apply { name = value }

    fun withPriceCents(value: Int) = apply { priceCents = value }

    fun withMunicipality(value: FareMunicipality) = apply { municipality = value.name }

    fun withTraveller(value: FareTraveller) = apply { traveller = value.name }

    fun withKind(value: FareKind) = apply { kind = value.name }

    fun withDescription(value: String?) = apply { description = value }

    fun withStoredAt(value: Long) = apply { storedAt = value }

    fun build(): FareEntity =
        FareEntity(
            code = code,
            name = name,
            priceCents = priceCents,
            municipality = municipality,
            traveller = traveller,
            kind = kind,
            description = description,
            storedAt = storedAt,
        )
}
