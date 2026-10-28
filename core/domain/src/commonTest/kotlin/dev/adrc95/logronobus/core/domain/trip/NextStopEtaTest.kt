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

package dev.adrc95.logronobus.core.domain.trip

import dev.adrc95.logronobus.core.model.Stop
import dev.adrc95.logronobus.core.model.StopEta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NextStopEtaTest {
    private fun stop(ref: String) =
        Stop(
            stopRef = ref,
            stopId = ref,
            longName = ref,
            shortName = ref,
            latitude = 53.27,
            longitude = -9.10,
        )

    private val stops = listOf(stop("A"), stop("B"), stop("C"))
    private val next = StopEta("13:59", upcoming = true)

    @Test
    fun `given a bus heading to a stop when the map bubble is built then that stop carries it`() {
        val etas = mapOf("A" to next, "B" to StopEta("14:02", upcoming = true))
        assertEquals("A" to next, stops.nextStopEta(etas, busIndex = 0))
    }

    @Test
    fun `given a time two stops ahead when the map bubble is built then it is not used`() {
        val etas = mapOf("B" to StopEta("14:02", upcoming = true))
        assertNull(
            stops.nextStopEta(etas, busIndex = 0),
            "Better no bubble at all than one on the wrong corner",
        )
    }

    @Test
    fun `given the stop the bus has just left when the bubble is built then its time is not shown`() {
        val etas =
            mapOf("A" to StopEta("", upcoming = false), "B" to StopEta("14:02", upcoming = true))
        assertEquals(
            "B" to StopEta("14:02", upcoming = true),
            stops.nextStopEta(etas, busIndex = 1),
        )
    }

    @Test
    fun `given a next stop without a live time when the map is built then there is no bubble`() {
        val etas =
            mapOf("A" to StopEta("", upcoming = false), "B" to StopEta("14:02", upcoming = true))
        assertNull(
            stops.nextStopEta(etas, busIndex = 0),
            "A is the next stop and it has nothing to say",
        )
    }

    @Test
    fun `given the bus at the end of the run when the map is built then no time is shown`() {
        assertNull(stops.nextStopEta(mapOf("A" to next), busIndex = stops.size))
    }

    @Test
    fun `given no bus when the next stop is asked then there is none`() {
        assertNull(stops.nextStopEta(mapOf("A" to next), busIndex = -1))
    }
}
