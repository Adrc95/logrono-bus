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

package dev.adrc95.logronobus.feature.scan

import app.cash.turbine.test
import dev.adrc95.logronobus.core.domain.usecase.AskForStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.ReadStopSignUseCase
import dev.adrc95.logronobus.core.model.Route
import dev.adrc95.logronobus.core.model.RouteDetail
import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.testing.MainDispatcherRule
import dev.adrc95.logronobus.core.testing.fake.FakeCatalogueRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScanViewModelTest {
    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun set_up() {
        mainDispatcherRule.setUp()
    }

    @AfterTest
    fun tear_down() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(detail: RouteDetail? = null) =
        ScanViewModel(
            readStopSign =
                ReadStopSignUseCase(
                    FakeCatalogueRepository(
                        routes =
                            mapOf(
                                LINE to
                                    Route(
                                        timetableId = LINE,
                                        longName = "6-CENTRO-EL CORTIJO",
                                        shortName = LINE,
                                        internalId = 6,
                                    ),
                            ),
                        stopsByRoute = listOf(stopsOf(townStops), stopsOf(countryStops)),
                        detail = detail,
                    ),
                ),
            askForStop = AskForStopUseCase(),
        )

    @Test
    fun `given no frame when the scanner opens then the state is empty`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val model = createViewModel()

            // Then
            model.uiState.test {
                val state = awaitItem()
                assertNull(state.line)
                assertNull(state.direction)
                assertNull(state.preview)
                assertNull(state.stop)
                assertNull(state.name)
                assertTrue(state.candidates.isEmpty())
                assertFalse(state.askForStop)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a sign with its line and a marked stop when the frame arrives then the line the direction and the stop are shown`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val model = createViewModel()

            // Then
            model.uiState.test {
                awaitItem()
                model.onPieces(signPieces(markedStop = MARKED_STOP))
                var state = awaitItem()
                while (state.stop?.name != MARKED_STOP) {
                    state = awaitItem()
                }
                assertEquals(LINE, state.line)
                assertEquals(0, state.direction)
                assertEquals(MARKED_STOP, state.preview)
                assertEquals("Centro - El Cortijo", state.name)
                assertEquals(6, state.candidates.size)
                assertFalse(state.askForStop)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a sign of the second direction when the frame arrives then the title shows that direction's origin and destination`() =
        runTest(mainDispatcherRule.scheduler) {
            // Given
            val model =
                createViewModel(
                    detail =
                        RouteDetail(
                            stops = listOf(stopsOf(townStops), stopsOf(countryStops)),
                            origins = listOf("EL CORTIJO", "CENTRO"),
                        ),
                )

            // Then
            model.uiState.test {
                awaitItem()
                model.onPieces(signPieces(stops = countryStops))
                var state = awaitItem()
                while (state.line == null) {
                    state = awaitItem()
                }
                assertEquals(1, state.direction)
                assertEquals("El Cortijo - Centro", state.name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given a sign without a marked stop when the frames keep coming then the rider is asked for the stop`() =
        runTest(mainDispatcherRule.scheduler) {
            // When
            val model = createViewModel()

            // Then
            model.uiState.test {
                awaitItem()
                repeat(5) { model.onPieces(signPieces()) }
                var state = awaitItem()
                while (!state.askForStop) {
                    state = awaitItem()
                }
                assertTrue(state.askForStop)
                assertNull(state.stop)
                assertNull(state.preview)
                assertEquals(LINE, state.line)
                assertEquals(6, state.candidates.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private const val LINE = "6"
private const val MARKED_STOP = "Calle Mayor"

private val townStops =
    listOf(
        "Plaza de la iglesia El Cortijo",
        "Avenida de la Paz",
        "Calle Mayor",
        "Puente de Piedra",
        "Estación de Autobuses",
        "Universidad",
    )

private val countryStops =
    listOf(
        "Barrio de Varea",
        "Molino Viejo",
        "Fuente del Prado",
        "Huerta Grande",
        "Camino Alto",
        "Casa Blanca",
    )

private fun stopsOf(names: List<String>): List<Stop> =
    names.mapIndexed { index, name ->
        Stop(
            stopRef = "$name-ref",
            stopId = "$index",
            longName = name,
            shortName = name,
            latitude = 42.46 + index * 0.001,
            longitude = -2.45 - index * 0.001,
        )
    }

private fun signPieces(
    markedStop: String? = null,
    stops: List<String> = townStops,
): List<ScannedText> =
    buildList {
        add(ScannedText(text = LINE, left = 10f, top = 0f, width = 12f, height = 20f))
        add(ScannedText(text = "CENTRO", left = 10f, top = 40f, width = 90f, height = 20f))
        add(ScannedText(text = "EL CORTIJO", left = 10f, top = 80f, width = 100f, height = 20f))
        stops.forEachIndexed { index, name ->
            add(
                ScannedText(
                    text = name,
                    left = 200f,
                    top = 300f + index * 40f,
                    width = 150f,
                    height = 20f,
                    colour = if (name == markedStop) 200 else 0,
                ),
            )
        }
    }
