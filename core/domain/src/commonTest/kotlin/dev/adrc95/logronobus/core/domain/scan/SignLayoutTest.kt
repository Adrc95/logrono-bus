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

package dev.adrc95.logronobus.core.domain.scan

import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.core.model.Stop
import kotlin.test.Test
import kotlin.test.assertEquals

class SignLayoutTest {
    private val stops =
        listOf(
            "Las Norias",
            "Graciano",
            "Ferial",
            "Blanco Lac",
            "Ronda de los Cuarteles",
            "La Cometa",
            "Beratúa",
            "Labradores",
            "Lardero",
            "Olimpia",
            "Monumento al Labrador",
            "Estación de Autobuses",
            "Huesca",
            "Club Deportivo",
            "Poeta Prudencio",
            "Gustavo Adolfo Bécquer",
            "Juan Boscán",
            "Consejo Regulador",
            "Tirso de Molina",
            "Linares",
            "Hospital San Pedro",
            "Puente Madre - Hospital",
            "Marianistas",
            "Jardines del Iregua",
            "Villacañas",
            "Valdecarros",
            "Ayuntamiento Villamediana",
            "San Cristóbal",
            "Piscinas",
            "Doña Hermesinda",
        )

    @Test
    fun `given a photo with the computer's menus when the sign is read then it is still read`() {
        val sign = pieces().toScannedSign()
        assertEquals("3", sign.line)
        assertEquals(listOf("LAS NORIAS", "VILLAMEDIANA"), sign.directions)
        assertEquals(stops, sign.stops.map { it.name })
        assertEquals(listOf("Olimpia"), sign.stops.filter { it.isMarked }.map { it.name })
    }

    @Test
    fun `given a sign with a marked stop when it is read then the marker is the catalogue's stop`() {
        val sign = pieces().toScannedSign()
        val catalogue =
            stops.map { name ->
                Stop(
                    stopRef = "ref-$name",
                    stopId = "",
                    longName = name,
                    shortName = name,
                    latitude = 0.0,
                    longitude = 0.0,
                )
            }
        assertEquals("Olimpia", findMarkedStop(sign, catalogue)?.longName)
        assertEquals(0, resolveDirection(sign, listOf(catalogue, catalogue.reversed()), emptyList()))
    }

    private fun pieces(): List<ScannedText> {
        val pieces = mutableListOf<ScannedText>()
        listOf(
            "Archivo" to 110f,
            "Edición" to 180f,
            "Visualización" to 250f,
            "Ventana" to 320f,
            "Ayuda" to 368f,
        ).forEach { (text, left) -> pieces += ScannedText(text, left, 4f, 80f, 30f) }
        pieces += ScannedText("3", 30f, 40f, 170f, 250f)
        pieces += ScannedText("LAS", 40f, 330f, 200f, 85f)
        pieces += ScannedText("NORIAS", 250f, 330f, 400f, 85f)
        pieces += ScannedText("VILLAMEDIANA", 40f, 470f, 620f, 85f)
        stops.forEachIndexed { index, name ->
            pieces +=
                ScannedText(
                    text = name,
                    left = 250f + index * 3f,
                    top = 40f + index * 50f,
                    width = name.length * 18f,
                    height = 18f,
                    colour = if (name == "Olimpia") 80 else 5,
                )
        }
        return pieces
    }
}
