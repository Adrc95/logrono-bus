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

import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.network.dto.FareProductDto

fun FareProductDto.toDomain(): Fare {
    val upper = name.uppercase()
    return Fare(
        code = productCode,
        name = name,
        priceCents = price ?: 0,
        municipality =
            when {
                upper.contains("LARDERO") -> FareMunicipality.LARDERO
                upper.contains("VILLAMEDIANA") -> FareMunicipality.VILLAMEDIANA
                else -> FareMunicipality.LOGRONO
            },
        traveller =
            when {
                upper.contains("JUBILADO") -> {
                    FareTraveller.SENIOR
                }

                upper.contains("ESTUDIANTE") -> {
                    FareTraveller.STUDENT
                }

                upper.contains("BONOPEQUE") || upper.contains("NIÑO") || upper.contains("NINO") -> {
                    FareTraveller.CHILD
                }

                upper.contains("FN50") -> {
                    FareTraveller.LARGE_FAMILY_50
                }

                upper.contains("FN20") -> {
                    FareTraveller.LARGE_FAMILY_20
                }

                upper.contains("REDUCIDA") -> {
                    FareTraveller.REDUCED
                }

                upper.contains("EMPLEADO") -> {
                    FareTraveller.EMPLOYEE
                }

                upper.contains("FAMILIAR") -> {
                    FareTraveller.FAMILY_MEMBER
                }

                else -> {
                    FareTraveller.GENERAL
                }
            },
        kind =
            when {
                upper.contains("BONOMES") -> FareKind.MONTH
                upper.contains("BONOBUS") || upper.contains("10 VIAJES") -> FareKind.TRIPS_10
                upper.contains("SENCILLO") -> FareKind.SINGLE
                else -> FareKind.OTHER
            },
        description = description?.trim()?.takeIf { it.isNotBlank() },
    )
}
