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

package dev.adrc95.logronobus.core.testing

import de.jensklingenberg.ktorfit.Ktorfit
import dev.adrc95.logronobus.core.data.departures.FleetSnapshotReader
import dev.adrc95.logronobus.core.data.departures.TimetableRowsReader
import dev.adrc95.logronobus.core.data.repository.CatalogueRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.DeparturesRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.FaresRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.FavouritesRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.LastViewedRouteRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.VehiclesRepositoryImpl
import dev.adrc95.logronobus.core.data.repository.WalkingRepositoryImpl
import dev.adrc95.logronobus.core.data.vehicles.NightArrivalEstimator
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.repository.FaresRepository
import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.domain.repository.LastViewedRouteRepository
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.repository.WalkingRepository
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.datasource.LogronoCatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.LogronoDeparturesDataSource
import dev.adrc95.logronobus.core.network.datasource.LogronoFaresDataSource
import dev.adrc95.logronobus.core.network.datasource.LogronoFleetDataSource
import dev.adrc95.logronobus.core.network.datasource.LogronoWalkingDataSource
import dev.adrc95.logronobus.core.network.service.CatalogueService
import dev.adrc95.logronobus.core.network.service.DeparturesService
import dev.adrc95.logronobus.core.network.service.FaresService
import dev.adrc95.logronobus.core.network.service.FleetService
import dev.adrc95.logronobus.core.network.service.WalkingService
import dev.adrc95.logronobus.core.network.service.createCatalogueService
import dev.adrc95.logronobus.core.network.service.createDeparturesService
import dev.adrc95.logronobus.core.network.service.createFaresService
import dev.adrc95.logronobus.core.network.service.createFleetService
import dev.adrc95.logronobus.core.network.service.createWalkingService
import dev.adrc95.logronobus.core.preference.createDataStore
import dev.adrc95.logronobus.core.preference.datasource.DataStorePreferencesDataSource
import dev.adrc95.logronobus.core.preference.datasource.PreferencesDataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val sharedLocalStore = InMemoryFavouritesDataSource()
private val sharedPreferencesStore: PreferencesDataSource =
    DataStorePreferencesDataSource(createDataStore())

class TestBus(
    injectedHttpClient: HttpClient? = null,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    private val client =
        injectedHttpClient ?: HttpClient {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
    private val ktorfit: Ktorfit =
        Ktorfit
            .Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .httpClient(client)
            .build()
    private val catalogueService: CatalogueService = ktorfit.createCatalogueService()
    private val departuresService: DeparturesService = ktorfit.createDeparturesService()
    private val fleetService: FleetService = ktorfit.createFleetService()
    private val faresService: FaresService = ktorfit.createFaresService()
    private val walkingService: WalkingService = ktorfit.createWalkingService()
    private val source = LogronoCatalogueDataSource(catalogueService)
    private val fleet = LogronoFleetDataSource(fleetService, source)
    private val remote = LogronoDeparturesDataSource(departuresService)
    private val local = sharedLocalStore

    val catalogue: CatalogueRepository = CatalogueRepositoryImpl(source, null, null)
    val departures: DeparturesRepository =
        DeparturesRepositoryImpl(
            remote = remote,
            catalogue = source,
            local = local,
            fleetViews = FleetSnapshotReader(fleet, source),
            timetable = TimetableRowsReader(remote, source, fleet),
        )
    val vehicles: VehiclesRepository =
        VehiclesRepositoryImpl(fleet, source, NightArrivalEstimator(remote))
    val fares: FaresRepository =
        FaresRepositoryImpl(LogronoFaresDataSource(faresService), null, null)
    val favourites: FavouritesRepository = FavouritesRepositoryImpl(local)
    val lastViewedRoute: LastViewedRouteRepository =
        LastViewedRouteRepositoryImpl(sharedPreferencesStore)
    val walking: WalkingRepository = WalkingRepositoryImpl(LogronoWalkingDataSource(walkingService))
}

fun fleetForLine(
    payload: String,
    lineId: String,
): String {
    val activities =
        Json
            .parseToJsonElement(payload)
            .jsonObject["result"]!!
            .jsonObject["activities"]!!
            .jsonArray
    val mine =
        activities.filter { it.jsonObject["lineRef"]?.jsonPrimitive?.contentOrNull == lineId }
    return Json.encodeToString(
        JsonObject.serializer(),
        buildJsonObject {
            put("result", buildJsonObject { put("activities", JsonArray(mine)) })
        },
    )
}
