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

import dev.adrc95.logronobus.core.model.Fare
import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.feature.fares.model.FareDisplayModel

fun List<Fare>.toDisplayModels(
    municipality: FareMunicipality?,
    traveller: FareTraveller,
): List<FareDisplayModel> =
    filter { fare ->
        (municipality == null || fare.municipality == municipality) &&
            (traveller == FareTraveller.ALL || fare.traveller == traveller)
    }.sortedBy { it.priceCents }
        .map { it.toDisplayModel() }

fun Fare.toDisplayModel(): FareDisplayModel =
    FareDisplayModel(
        code = code,
        name = name,
        priceLabel = priceLabel(),
        description = description,
    )

private fun Fare.priceLabel(): String {
    val euros = priceCents / 100
    val cents = (priceCents % 100).toString().padStart(2, '0')
    return "$euros,$cents €"
}
