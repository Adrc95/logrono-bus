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

package dev.adrc95.logronobus.feature.fares.model

import dev.adrc95.logronobus.core.model.FareMunicipality
import dev.adrc95.logronobus.core.model.FareTraveller
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.fare_municipality_all
import dev.adrc95.logronobus.core.ui.generated.resources.fare_municipality_lardero
import dev.adrc95.logronobus.core.ui.generated.resources.fare_municipality_logrono
import dev.adrc95.logronobus.core.ui.generated.resources.fare_municipality_villamediana
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_all
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_estudiante
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_fn20
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_fn50
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_general
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_jubilado
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_nino
import dev.adrc95.logronobus.core.ui.generated.resources.fare_traveller_reducida
import dev.adrc95.logronobus.feature.fares.FareConstants

val fareTravellerOptions: List<FareTravellerDisplayModel> =
    listOf(
        FareTravellerDisplayModel(FareTraveller.ALL.name, Res.string.fare_traveller_all),
        FareTravellerDisplayModel(FareTraveller.GENERAL.name, Res.string.fare_traveller_general),
        FareTravellerDisplayModel(FareTraveller.STUDENT.name, Res.string.fare_traveller_estudiante),
        FareTravellerDisplayModel(FareTraveller.SENIOR.name, Res.string.fare_traveller_jubilado),
        FareTravellerDisplayModel(FareTraveller.LARGE_FAMILY_20.name, Res.string.fare_traveller_fn20),
        FareTravellerDisplayModel(FareTraveller.LARGE_FAMILY_50.name, Res.string.fare_traveller_fn50),
        FareTravellerDisplayModel(FareTraveller.CHILD.name, Res.string.fare_traveller_nino),
        FareTravellerDisplayModel(FareTraveller.REDUCED.name, Res.string.fare_traveller_reducida),
    )

val fareMunicipalityOptions: List<FareMunicipalityDisplayModel> =
    listOf(
        FareMunicipalityDisplayModel(FareConstants.ALL_MUNICIPALITIES_ID, Res.string.fare_municipality_all),
        FareMunicipalityDisplayModel(FareMunicipality.LOGRONO.name, Res.string.fare_municipality_logrono),
        FareMunicipalityDisplayModel(FareMunicipality.LARDERO.name, Res.string.fare_municipality_lardero),
        FareMunicipalityDisplayModel(
            FareMunicipality.VILLAMEDIANA.name,
            Res.string.fare_municipality_villamediana,
        ),
    )
