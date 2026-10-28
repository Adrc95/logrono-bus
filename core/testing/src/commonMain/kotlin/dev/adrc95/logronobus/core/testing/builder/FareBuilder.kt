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

package dev.adrc95.logronobus.core.testing.builder

import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller

fun fare(block: FareBuilder.() -> Unit = {}): Fare = FareBuilder().apply(block).build()

class FareBuilder {
    private var code: String = "SINGLE"
    private var name: String = "Billete sencillo"
    private var priceCents: Int = 130
    private var municipality: FareMunicipality = FareMunicipality.LOGRONO
    private var traveller: FareTraveller = FareTraveller.GENERAL
    private var kind: FareKind = FareKind.SINGLE
    private var description: String? = null

    fun withCode(value: String) = apply { code = value }

    fun withName(value: String) = apply { name = value }

    fun withPriceCents(value: Int) = apply { priceCents = value }

    fun withMunicipality(value: FareMunicipality) = apply { municipality = value }

    fun withTraveller(value: FareTraveller) = apply { traveller = value }

    fun withKind(value: FareKind) = apply { kind = value }

    fun withDescription(value: String?) = apply { description = value }

    fun build(): Fare =
        Fare(
            code = code,
            name = name,
            priceCents = priceCents,
            municipality = municipality,
            traveller = traveller,
            kind = kind,
            description = description,
        )
}
