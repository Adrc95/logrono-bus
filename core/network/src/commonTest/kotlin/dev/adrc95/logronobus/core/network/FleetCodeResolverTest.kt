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

import dev.adrc95.logronobus.core.network.builder.lineDto
import dev.adrc95.logronobus.core.network.datasource.LogronoCatalogueDataSource
import dev.adrc95.logronobus.core.network.dto.ApiLinesResponse
import dev.adrc95.logronobus.core.network.dto.ApiLinesResult
import dev.adrc95.logronobus.core.network.dto.ApiStopsResponse
import dev.adrc95.logronobus.core.network.dto.ApiStopsResult
import dev.adrc95.logronobus.core.network.dto.LineDto
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FleetCodeResolverTest {
    @Test
    fun `given the fleet reports a ref the catalogue does not list when resolved then the ref is kept as it came`() =
        runTest {
            val resolve =
                resolverWith(
                    lineDto {
                        withId(31)
                        withName("B1-VAREA-LA CAVA")
                    },
                )

            assertEquals("13", resolve("13", null))
        }

    @Test
    fun `given the fleet reports the current night ref when resolved then the catalogue code wins`() =
        runTest {
            val resolve =
                resolverWith(
                    lineDto {
                        withId(31)
                        withName("B1-VAREA-LA CAVA")
                    },
                    lineDto {
                        withId(32)
                        withName("B2-EL ARCO-LA ESTRELLA")
                    },
                )

            assertEquals("B1", resolve("31", null))
            assertEquals("B2", resolve("32", null))
        }

    @Test
    fun `given the catalogue has a line with that ref when resolved then the catalogue code wins over the alias`() =
        runTest {
            val resolve =
                resolverWith(
                    lineDto {
                        withId(12)
                        withName("12-LARDERO-HOSPITAL SAN PEDRO")
                    },
                    lineDto {
                        withId(31)
                        withName("B1-VAREA-LA CAVA")
                    },
                )

            assertEquals("12", resolve("12", null))
        }

    @Test
    fun `given the fleet publishes a known line name when resolved then the catalogue code wins`() =
        runTest {
            val resolve =
                resolverWith(
                    lineDto {
                        withId(31)
                        withName("B1-VAREA-LA CAVA")
                    },
                )

            assertEquals("B1", resolve("99", "VAREA-LA CAVA"))
        }

    private suspend fun resolverWith(vararg lines: LineDto): (String, String?) -> String? = LogronoCatalogueDataSource(FakeCatalogueService(lines = lines.toList())).fleetCodeResolver()
}
