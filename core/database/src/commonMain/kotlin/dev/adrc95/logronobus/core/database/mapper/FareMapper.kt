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

import dev.adrc95.logronobus.core.database.entity.FareEntity
import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.model.FareKind
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller

fun Fare.toEntity(): FareEntity =
    FareEntity(
        code = code,
        name = name,
        priceCents = priceCents,
        municipality = municipality.name,
        traveller = traveller.name,
        kind = kind.name,
        description = description,
    )

fun FareEntity.toDomain(): Fare =
    Fare(
        code = code,
        name = name,
        priceCents = priceCents,
        municipality = FareMunicipality.valueOf(municipality),
        traveller = FareTraveller.fromId(traveller),
        kind = FareKind.valueOf(kind),
        description = description,
    )
