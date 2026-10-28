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

package dev.adrc95.logronobus.core.network.di

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory
import dev.adrc95.logronobus.core.network.NetworkConstants
import dev.adrc95.logronobus.core.network.engine.httpClientEngineFactory
import dev.adrc95.logronobus.core.network.installServerClock
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("dev.adrc95.logronobus.core.network")
class NetworkModule {
    @Single
    @Named("baseUrl")
    fun provideBaseUrl(): String = NetworkConstants.BASE_URL

    @Single
    fun provideJson(): Json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Single
    fun provideHttpClient(json: Json): HttpClient =
        HttpClient(httpClientEngineFactory()) {
            expectSuccess = true
            install(HttpTimeout) {
                connectTimeoutMillis = NetworkConstants.CONNECT_TIMEOUT_MILLIS
                requestTimeoutMillis = NetworkConstants.REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = NetworkConstants.SOCKET_TIMEOUT_MILLIS
            }
            install(HttpRequestRetry) {
                maxRetries = NetworkConstants.RETRY_ATTEMPTS - 1
                delayMillis { attempt -> NetworkConstants.RETRY_BASE_DELAY_MILLIS * (attempt + 1) }
                retryOnExceptionOrServerErrors(maxRetries)
            }
            install(ContentNegotiation) {
                json(json)
            }
            installServerClock()
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpCache)
        }

    @Single
    fun provideKtorfit(
        @Named("baseUrl") baseUrl: String,
        httpClient: HttpClient,
    ): Ktorfit =
        Ktorfit
            .Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .converterFactories(ResponseConverterFactory())
            .build()

    @Single
    fun provideCatalogueService(ktorfit: Ktorfit): CatalogueService = ktorfit.createCatalogueService()

    @Single
    fun provideDeparturesService(ktorfit: Ktorfit): DeparturesService = ktorfit.createDeparturesService()

    @Single
    fun provideFleetService(ktorfit: Ktorfit): FleetService = ktorfit.createFleetService()

    @Single
    fun provideFaresService(ktorfit: Ktorfit): FaresService = ktorfit.createFaresService()

    @Single
    fun provideWalkingService(ktorfit: Ktorfit): WalkingService = ktorfit.createWalkingService()
}
