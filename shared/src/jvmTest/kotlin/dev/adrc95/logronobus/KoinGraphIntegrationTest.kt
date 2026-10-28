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

package dev.adrc95.logronobus

import androidx.compose.ui.test.v2.runComposeUiTest
import dev.adrc95.logronobus.core.database.LogronoDatabase
import dev.adrc95.logronobus.core.database.datasource.FavouritesDataSource
import dev.adrc95.logronobus.core.device.AppPermissions
import dev.adrc95.logronobus.core.device.LocationProvider
import dev.adrc95.logronobus.core.device.Platform
import dev.adrc95.logronobus.core.domain.repository.CatalogueRepository
import dev.adrc95.logronobus.core.domain.repository.DeparturesRepository
import dev.adrc95.logronobus.core.domain.repository.FaresRepository
import dev.adrc95.logronobus.core.domain.repository.FavouritesRepository
import dev.adrc95.logronobus.core.domain.repository.SettingsRepository
import dev.adrc95.logronobus.core.domain.repository.VehiclesRepository
import dev.adrc95.logronobus.core.domain.repository.WalkingRepository
import dev.adrc95.logronobus.core.domain.usecase.AddFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetBusPositionsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetFareProductsUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetRouteDetailUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopDeparturesUseCase
import dev.adrc95.logronobus.core.domain.usecase.GetStopsUseCase
import dev.adrc95.logronobus.core.domain.usecase.RemoveFavouriteUseCase
import dev.adrc95.logronobus.core.domain.usecase.SaveFavouriteStopsUseCase
import dev.adrc95.logronobus.core.network.datasource.CatalogueDataSource
import dev.adrc95.logronobus.core.network.datasource.DeparturesRemoteDataSource
import dev.adrc95.logronobus.core.network.datasource.FaresDataSource
import dev.adrc95.logronobus.core.network.datasource.FleetDataSource
import dev.adrc95.logronobus.core.network.datasource.WalkingDataSource
import dev.adrc95.logronobus.core.network.service.CatalogueService
import dev.adrc95.logronobus.core.network.service.DeparturesService
import dev.adrc95.logronobus.core.network.service.FaresService
import dev.adrc95.logronobus.core.network.service.FleetService
import dev.adrc95.logronobus.core.network.service.WalkingService
import dev.adrc95.logronobus.core.preference.datasource.PreferencesDataSource
import dev.adrc95.logronobus.core.testing.EMPTY_FEEDS
import dev.adrc95.logronobus.di.initKoin
import dev.adrc95.logronobus.feature.buses.BusesViewModel
import dev.adrc95.logronobus.feature.fares.FaresViewModel
import dev.adrc95.logronobus.feature.favourites.FavouritesViewModel
import dev.adrc95.logronobus.feature.nearby.NearbyViewModel
import dev.adrc95.logronobus.feature.routes.RoutesViewModel
import dev.adrc95.logronobus.feature.settings.SettingsViewModel
import dev.adrc95.logronobus.feature.stops.StopsSheetViewModel
import dev.adrc95.logronobus.feature.stops.api.navigation.StopSheetNavKey
import dev.adrc95.logronobus.feature.tracking.TrackingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class KoinGraphIntegrationTest {
    private val koin: Koin
        get() = assertNotNull(KoinPlatform.getKoinOrNull(), "initKoin() did not start a graph")

    private val mockClient =
        HttpClient(
            MockEngine {
                respond(
                    content = EMPTY_FEEDS,
                    status = HttpStatusCode.OK,
                    headers =
                        headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString(),
                        ),
                )
            },
        )

    @BeforeTest
    fun startTheGraph() {
        KoinPlatform.stopKoin()
        initKoin {
            allowOverride(true)
            modules(
                module {
                    single<HttpClient> { mockClient }
                },
            )
        }
    }

    @AfterTest
    fun stopTheGraph() {
        KoinPlatform.stopKoin()
    }

    @Test
    fun `given the graph when data sources and repositories are asked for then they all resolve`() {
        assertSame(mockClient, koin.get<HttpClient>())
        assertNotNull(koin.get<CatalogueService>())
        assertNotNull(koin.get<DeparturesService>())
        assertNotNull(koin.get<FleetService>())
        assertNotNull(koin.get<FaresService>())
        assertNotNull(koin.get<WalkingService>())
        assertNotNull(koin.get<CatalogueDataSource>())
        assertNotNull(koin.get<DeparturesRemoteDataSource>())
        assertNotNull(koin.get<FleetDataSource>())
        assertNotNull(koin.get<FaresDataSource>())
        assertNotNull(koin.get<WalkingDataSource>())
        assertNotNull(koin.get<FavouritesDataSource>())
        assertNotNull(koin.get<LogronoDatabase>())
        assertNotNull(koin.get<PreferencesDataSource>())
        assertNotNull(koin.get<Platform>())
        assertNotNull(koin.get<AppPermissions>())
        assertNotNull(koin.get<LocationProvider>())
        assertNotNull(koin.get<CatalogueRepository>())
        assertNotNull(koin.get<DeparturesRepository>())
        assertNotNull(koin.get<VehiclesRepository>())
        assertNotNull(koin.get<FaresRepository>())
        assertNotNull(koin.get<FavouritesRepository>())
        assertNotNull(koin.get<SettingsRepository>())
        assertNotNull(koin.get<WalkingRepository>())
    }

    @Test
    fun `given the graph when repositories are built then each caller gets its own and the data sources are shared`() {
        assertNotSame(koin.get<CatalogueRepository>(), koin.get<CatalogueRepository>())
        assertSame(koin.get<CatalogueDataSource>(), koin.get<CatalogueDataSource>())
        assertSame(koin.get<FleetDataSource>(), koin.get<FleetDataSource>())
        assertSame(koin.get<FavouritesDataSource>(), koin.get<FavouritesDataSource>())
        assertSame(koin.get<PreferencesDataSource>(), koin.get<PreferencesDataSource>())
    }

    @Test
    fun `given the graph when the screens' use cases are asked for then they all resolve`() {
        assertNotNull(koin.get<GetStopsUseCase>())
        assertNotNull(koin.get<GetRouteDetailUseCase>())
        assertNotNull(koin.get<GetStopDeparturesUseCase>())
        assertNotNull(koin.get<GetBusPositionsUseCase>())
        assertNotNull(koin.get<GetFareProductsUseCase>())
        assertNotNull(koin.get<AddFavouriteUseCase>())
        assertNotNull(koin.get<RemoveFavouriteUseCase>())
        assertNotNull(koin.get<SaveFavouriteStopsUseCase>())
    }

    @Test
    fun `given the graph when each screen's view model is asked for then it resolves`() {
        assertNotNull(koin.get<FaresViewModel>())
        assertNotNull(koin.get<BusesViewModel>())
        assertNotNull(koin.get<NearbyViewModel>())
        assertNotNull(koin.get<FavouritesViewModel>())
        assertNotNull(koin.get<SettingsViewModel>())
        assertNotNull(koin.get<StopsSheetViewModel> { parametersOf(StopSheetNavKey("360")) })
        assertNotNull(koin.get<TrackingViewModel>())
    }

    @Test
    fun `given the graph when the routes view model is asked for on its own then it resolves`() {
        val routes = koin.get<RoutesViewModel>()
        assertNotNull(routes)
    }

    @Test
    fun `given a screen when it asks for its view model the koin way then it resolves`() =
        runComposeUiTest {
            var stopsSheet: StopsSheetViewModel? = null
            var routes: RoutesViewModel? = null
            setContent {
                val sheet = koinViewModel<StopsSheetViewModel> { parametersOf(StopSheetNavKey("360")) }
                val tracking = koinViewModel<TrackingViewModel>()
                stopsSheet = sheet
                routes = koinViewModel<RoutesViewModel>()
            }
            waitForIdle()
            assertNotNull(stopsSheet)
            assertNotNull(routes)
        }

    @Test
    fun `given a started graph when it is started again then the graph is not replaced`() {
        val first = koin
        runCatching { initKoin() }
        assertSame(first, KoinPlatform.getKoinOrNull(), "the first graph is untouched")
    }
}
