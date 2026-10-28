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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun stubbedBackend(): HttpClient {
    val engine =
        MockEngine { request ->
            val body =
                when {
                    request.url.encodedPath.endsWith("/api/linesDiscovery/stops") -> STOPS_JSON
                    request.url.encodedPath.endsWith("/api/linesDiscovery/lines") -> LINES_JSON
                    request.url.encodedPath.contains("/api/estimatedTimetable/byStop/") -> STOP_TIMES_JSON
                    request.url.encodedPath.endsWith("/api/vehicleMonitoring/all") -> """{"result":{"activities":[]}}"""
                    else -> EMPTY_FEEDS
                }
            respond(
                body,
                headers =
                    headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString(),
                    ),
            )
        }
    return HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
}

fun failingBackend(): HttpClient {
    val engine = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }
    return HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        expectSuccess = true
    }
}
