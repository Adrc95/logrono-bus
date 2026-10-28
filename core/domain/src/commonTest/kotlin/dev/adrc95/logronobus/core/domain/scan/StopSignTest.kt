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

import dev.adrc95.logronobus.core.model.Stop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StopSignTest {
    private val line7Stops =
        listOf(
            "Naval",
            "Candado",
            "Almendros",
            "Rectorado",
            "Noveno Centenario",
            "Hospital San Millán",
            "Santos Ascarza",
            "Ayuntamiento",
            "Portales",
            "Banco de España",
            "Torre de Logroño",
            "Daniel Trevijano",
            "Gonzalo de Berceo",
            "Estella",
            "Conservatorio",
            "Paula Montar",
            "Portillejo",
            "Camino de Santiago",
            "Toledo",
            "Enrique Granados",
        )

    private val line10Stops =
        listOf(
            "Manuel de Falla",
            "Enrique Granados",
            "Toledo",
            "Camino de Santiago",
            "Portillejo",
            "Estanque",
            "Pepe Blanco",
            "Plaza 1º de Mayo",
            "Vitoria",
            "Lardero",
            "Olimpia",
            "Banco de España",
            "Glorieta Doctor Zubía",
            "Ayuntamiento",
            "Santos Ascarza",
            "Hospital San Millán",
            "Rectorado",
            "Quevedo",
            "Baltasar Gracián",
            "Tirso de Molina",
            "Linares",
            "Hospital San Pedro",
            "Puente Madre-Hospital",
            "Marianistas",
            "Segador",
            "Serón",
            "La Cadena",
            "Plaza del Cerrado",
            "Iglesia de Varea",
            "Artesanos",
        )

    private val line1Stops =
        listOf(
            "Artesanos",
            "La Cadena",
            "La Fombera",
            "Los Lirios",
            "Rectorado",
            "Magisterio",
            "Politécnico",
            "Martín Ballesteros",
            "Palacio de Congresos",
            "Fermín Gurbindo",
            "San Millán",
            "Eliseo Pinedo",
            "Marqués de la Ensenada",
            "Monumento al Labrador",
            "Torre de Logroño",
            "Daniel Trevijano",
            "Vitoria",
            "Plaza 1º de Mayo",
            "Pepe Blanco",
            "Parque San Miguel",
            "Alfonso VI",
            "La Cava",
        )

    private val line6Stops =
        listOf(
            "Eliseo Pinedo",
            "Marqués de la Ensenada",
            "Monumento al labrador",
            "Torre de Logroño",
            "Daniel Trevijano",
            "Fuente de Murrieta",
            "Navarrete el Mudo",
            "Batalla de Clavijo",
            "El Cubo",
            "Blanco Lac",
            "La Isla",
            "Monte Rincón",
            "Viveros Municipales",
            "Plaza de la Iglesia",
        )

    @Test
    fun `given a line's sign when it is read then it gives the line both directions and its stops`() {
        val sign =
            signPieces(
                line = "7",
                directions = listOf("POLÍGONO CANTABRIA", "EL ARCO"),
                stops = line7Stops,
                marked = "Estella",
            ).toScannedSign()
        assertEquals("7", sign.line)
        assertEquals(listOf("POLÍGONO CANTABRIA", "EL ARCO"), sign.directions)
        assertEquals(line7Stops, sign.stops.map { it.name })
        assertEquals(listOf("Estella"), sign.stops.filter { it.isMarked }.map { it.name })
    }

    @Test
    fun `given a sign without a marked stop when it is read then it still gives the stops`() {
        val sign =
            signPieces(
                line = "6",
                directions = listOf("CENTRO", "EL CORTIJO"),
                stops = line6Stops,
            ).toScannedSign()
        assertEquals("6", sign.line)
        assertEquals(listOf("CENTRO", "EL CORTIJO"), sign.directions)
        assertEquals(line6Stops.size, sign.stops.size)
        assertTrue(sign.stops.none { it.isMarked })
    }

    @Test
    fun `given a sign when its direction is resolved then the one whose ends match answers`() {
        val sign =
            signPieces(
                line = "10",
                directions = listOf("EL ARCO", "HOSPITAL SAN PEDRO"),
                stops = line10Stops,
                marked = "Olimpia",
            ).toScannedSign()
        val direction =
            resolveDirection(
                sign = sign,
                stopsByDirection =
                    listOf(
                        line10Stops.reversed().map { stop(it) },
                        line10Stops.map { stop(it) },
                    ),
                origins = listOf("Hospital San Pedro", "El Arco"),
            )
        assertEquals(1, direction)
    }

    @Test
    fun `given a night line when it is named then it is a buho with the B in front`() {
        val sign =
            signPieces(
                line = "1",
                nightName = "BÜHO",
                directions = listOf("VAREA", "LA CAVA"),
                stops = line1Stops,
                marked = "Torre de Logroño",
            ).toScannedSign()
        assertEquals("B1", sign.line)
        assertEquals(listOf("VAREA", "LA CAVA"), sign.directions)
        assertEquals(line1Stops.size, sign.stops.size)
    }

    @Test
    fun `given a night line's sign when it is read then it gives its stops too`() {
        val sign =
            signPieces(
                line = "1",
                directions = listOf("VAREA", "LA CAVA"),
                stops = line1Stops,
                marked = "Torre de Logroño",
            ).toScannedSign()
        assertEquals("1", sign.line)
        assertEquals(line1Stops.size, sign.stops.size)
        assertEquals(listOf("Torre de Logroño"), sign.stops.filter { it.isMarked }.map { it.name })
    }

    @Test
    fun `given a marked stop when the candidates are read then the marked one is found`() {
        val sign =
            signPieces(
                line = "10",
                directions = listOf("EL ARCO", "HOSPITAL SAN PEDRO"),
                stops = line10Stops,
                marked = "Olimpia",
            ).toScannedSign()
        val candidates = line10Stops.map { stop(it) }
        val found = findMarkedStop(sign, candidates)
        assertEquals("Olimpia", found?.longName)
    }

    @Test
    fun `given a sign without a marked stop when it is read then the rider chooses`() {
        val sign =
            signPieces(
                line = "6",
                directions = listOf("CENTRO", "EL CORTIJO"),
                stops = line6Stops,
            ).toScannedSign()
        assertNull(findMarkedStop(sign, line6Stops.map { stop(it) }))
    }

    @Test
    fun `given two names with different case or accents when they are compared then they match`() {
        assertTrue(namesMatch("Plaza 1º de Mayo", "Plaza 1º de Mayo"))
        assertTrue(namesMatch("HOSPITAL SAN MILLÁN", "Hospital San Millan"))
        assertTrue(namesMatch("Estella (Vía Centro)", "Estella"))
        assertFalse(namesMatch("San Adrián", "San Adrián Olimpia"))
        assertTrue(namesMatch("Torre de Logroño", "Torre de Logroño "))
        assertFalse(namesMatch("El Cubo", "La Isla"))
    }

    private fun stop(name: String) =
        Stop(
            stopRef = "ref-$name",
            stopId = "",
            longName = name,
            shortName = name,
            latitude = 0.0,
            longitude = 0.0,
        )
}
