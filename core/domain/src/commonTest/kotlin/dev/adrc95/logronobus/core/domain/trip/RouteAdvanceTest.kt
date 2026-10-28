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

import dev.adrc95.logronobus.core.domain.geo.distanceTo
import dev.adrc95.logronobus.core.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteAdvanceTest {
    private val firstStreet = GeoPoint(42.4655, -2.4443)
    private val corner = GeoPoint(42.4655, -2.4430)
    private val secondStreet = GeoPoint(42.4668, -2.4430)
    private val route = listOf(firstStreet, corner, secondStreet)

    @Test
    fun `given a corner in the shape when the bus advances then it follows the street instead of cutting across`() {
        val drawn = advanceAlongRoute(route, from = firstStreet, to = secondStreet, progress = 0.5)

        assertTrue(
            drawn.distanceTo(corner) < 30.0,
            "halfway along the route is by the corner, but the drawing was ${drawn.distanceTo(corner)} m from it",
        )
        val asTheCrowFlies = GeoPoint(42.46615, -2.44365)
        assertTrue(
            drawn.distanceTo(asTheCrowFlies) > 40.0,
            "and that is nowhere near the middle of the straight line, which is what cut the corners",
        )
    }

    @Test
    fun `given a bus with no measurable pace when it is drawn then it stands still`() {
        assertEquals(
            0.0,
            metresToDraw(observedMetresPerSecond = 0.0, elapsedMs = 45_000, remainingMetres = 400.0),
        )
    }

    @Test
    fun `given a bus heading to a stop when it is drawn then it never runs past it`() {
        assertEquals(
            120.0,
            metresToDraw(
                observedMetresPerSecond = 15.0,
                elapsedMs = 60_000,
                remainingMetres = 120.0,
            ),
        )
    }

    @Test
    fun `given a pace no bus can make when it is read then it is discarded`() {
        assertEquals(
            200.0,
            metresToDraw(
                observedMetresPerSecond = 100.0,
                elapsedMs = 10_000,
                remainingMetres = 10_000.0,
            ),
        )
    }

    @Test
    fun `given a single report when it ages then the bus stops moving`() {
        assertEquals(30_000L, extrapolationCapMs(gapBetweenReportsMs = 30_000))
        assertEquals(10_000L, extrapolationCapMs(gapBetweenReportsMs = 2_000))
        assertEquals(45_000L, extrapolationCapMs(gapBetweenReportsMs = 120_000))
    }

    @Test
    fun `given two points at a corner when they are compared then the distance is measured along the street`() {
        assertEquals(0.0, routeMetresTo(route, firstStreet))
        assertTrue(routeMetresTo(route, corner) < routeMetresTo(route, secondStreet))
        assertEquals(107.0, routeMetresTo(route, corner), absoluteTolerance = 1.0)
    }

    @Test
    fun `given two points the shape cannot order when they are compared then the straight line answers`() {
        val drawn = advanceAlongRoute(route, from = secondStreet, to = firstStreet, progress = 0.5)
        val middle =
            GeoPoint(
                lat = (secondStreet.lat + firstStreet.lat) / 2,
                lon = (secondStreet.lon + firstStreet.lon) / 2,
            )
        assertTrue(drawn.distanceTo(middle) < 1.0, "expected the straight line, got $drawn")
    }

    @Test
    fun `given a bus that has not moved when its pace is read then there is none`() {
        assertEquals(8.0, observedPace(metresAlongRoute = 240.0, seconds = 30.0))
        assertEquals(
            0.0,
            observedPace(metresAlongRoute = 0.0, seconds = 30.0),
            "a bus at a stop has shown no pace",
        )
        assertEquals(
            0.0,
            observedPace(metresAlongRoute = 240.0, seconds = 0.0),
            "no clock, no pace",
        )
    }

    @Test
    fun `given a report further back when it is drawn then it never goes backwards`() {
        assertTrue(
            drawingMayMoveTo(candidateMetres = 500.0, drawnBeforeMetres = 400.0),
            "ahead: draw it",
        )
        assertEquals(
            false,
            drawingMayMoveTo(candidateMetres = 380.0, drawnBeforeMetres = 400.0),
            "a report the screen had outrun: hold still",
        )
        assertEquals(
            false,
            drawingMayMoveTo(candidateMetres = 400.0, drawnBeforeMetres = 900.0),
            "half a kilometre back used to win as another branch; it is a different trip now, and " +
                "that resets the drawing instead of reversing it",
        )
    }

    @Test
    fun `given no shape when a distance is measured then the straight line answers`() {
        val drawn =
            advanceAlongRoute(emptyList(), from = firstStreet, to = secondStreet, progress = 0.5)
        assertTrue(drawn.lat > firstStreet.lat && drawn.lat < secondStreet.lat)
        assertEquals(-2.44365, drawn.lon, absoluteTolerance = 0.0001)
    }
}
