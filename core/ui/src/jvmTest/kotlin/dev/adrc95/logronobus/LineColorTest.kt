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

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import dev.adrc95.logronobus.core.designsystem.theme.readableOnColor
import dev.adrc95.logronobus.core.ui.util.toLineColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LineColorTest {
    @Test
    fun `given a catalogue rgba string when it is parsed then it becomes its colour`() {
        val red = "rgba(253,2,0,1)".toLineColor()!!
        assertEquals(253 / 255f, red.red, 1e-3f)
        assertEquals(2 / 255f, red.green, 1e-3f)
        assertEquals(0f, red.blue, 1e-3f)
        assertEquals(1f, red.alpha, 1e-3f, "alpha 1 in the string means fully opaque")

        val teal = "rgba(1,255,1,1)".toLineColor()!!
        assertEquals(1 / 255f, teal.red, 1e-3f)
        assertEquals(1f, teal.green, 1e-3f)
    }

    @Test
    fun `given channels out of range when they are parsed then they are clamped rather than thrown away`() {
        val clamped = "rgba(300,-5,0,1)".toLineColor()!!
        assertEquals(1f, clamped.red, 1e-3f)
        assertEquals(0f, clamped.green, 1e-3f)
    }

    @Test
    fun `given a colour with no alpha when it is parsed then it is opaque`() {
        assertEquals(1f, "rgba(1,2,3)".toLineColor()!!.alpha, 1e-4f)
    }

    @Test
    fun `given a string that is not a colour when it is parsed then there is none`() {
        assertNull(null.toLineColor())
        assertNull("".toLineColor())
        assertNull("#ff0000".toLineColor())
        assertNull("rgba(1,2)".toLineColor())
        assertNull("rgba(a,b,c)".toLineColor())
        assertNull("rgb(1,2,3)".toLineColor())
    }

    @Test
    fun `given every colour of the palette when the badge text is painted then it stays readable`() {
        assertEquals(Color.Black, "rgba(255,255,0,1)".toLineColor()!!.readableOnColor())
        assertEquals(Color.Black, "rgba(255,224,125,1)".toLineColor()!!.readableOnColor())
        assertEquals(Color.White, "rgba(253,2,0,1)".toLineColor()!!.readableOnColor())
        assertEquals(Color.White, "rgba(146,76,72,1)".toLineColor()!!.readableOnColor())
        assertEquals(Color.Black, Color.White.readableOnColor())
        assertEquals(Color.White, Color.Black.readableOnColor())
    }
}
