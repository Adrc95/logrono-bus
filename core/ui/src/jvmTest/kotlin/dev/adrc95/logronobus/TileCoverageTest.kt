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

import dev.adrc95.logronobus.core.ui.map.TileId
import dev.adrc95.logronobus.core.ui.map.ancestorCrop
import dev.adrc95.logronobus.core.ui.map.inWorld
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileCoverageTest {
    @Test
    fun `given a tile when its parent is read then it is its quadrant`() {
        val crop = ancestorCrop(tileX = 10, tileY = 7, levels = 1, ancestorWidth = 256)
        assertEquals(0, crop.offsetX)
        assertEquals(128, crop.offsetY)
        assertEquals(128, crop.size)
    }

    @Test
    fun `given a tile when its grandparent is read then the same rule applies on a 4x4 grid`() {
        val crop = ancestorCrop(tileX = 10, tileY = 7, levels = 2, ancestorWidth = 256)
        assertEquals(128, crop.offsetX)
        assertEquals(192, crop.offsetY)
        assertEquals(64, crop.size)
    }

    @Test
    fun `given a crop when it is taken then it stays inside the ancestor bitmap`() {
        val width = 256
        for (levels in 1..2) {
            val factor = 1 shl levels
            for (x in 0 until factor) {
                for (y in 0 until factor) {
                    val crop =
                        ancestorCrop(tileX = x, tileY = y, levels = levels, ancestorWidth = width)
                    assertTrue(crop.offsetX >= 0 && crop.offsetY >= 0, "negative crop at $x,$y")
                    assertTrue(crop.offsetX + crop.size <= width, "crop past the right edge at $x,$y")
                    assertTrue(crop.offsetY + crop.size <= width, "crop past the bottom edge at $x,$y")
                }
            }
        }
    }

    @Test
    fun `given a child tile when it is cropped then the crop matches where it sits`() {
        val parent = ancestorCrop(tileX = 6, tileY = 5, levels = 1, ancestorWidth = 256)
        val sibling = ancestorCrop(tileX = 7, tileY = 5, levels = 1, ancestorWidth = 256)
        assertEquals(parent.offsetY, sibling.offsetY)
        assertEquals(parent.offsetX + parent.size, sibling.offsetX)
    }

    @Test
    fun `given a tile outside the world when it is requested then it is skipped`() {
        assertTrue(TileId(12, 2048, 1500).inWorld())
        assertFalse(TileId(12, 4096, 0).inWorld(), "x past the edge of the world at zoom 12")
        assertFalse(TileId(12, 0, -1).inWorld())
        assertFalse(TileId(9, 10, 10).inWorld(), "below the minimum zoom the map allows")
        assertFalse(TileId(19, 10, 10).inWorld(), "above the maximum zoom the map allows")
    }
}
