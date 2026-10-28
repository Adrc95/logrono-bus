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

package dev.adrc95.logronobus.core.ui.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal fun DrawScope.drawTileOrAncestor(
    id: TileId,
    topLeft: Offset,
    tiles: Map<TileId, ImageBitmap>,
) {
    val loaded = tiles[id]
    if (loaded != null) {
        drawImage(loaded, topLeft = topLeft)
        return
    }
    var levels = 1
    while (levels <= 2 && id.z - levels >= 0) {
        val ancestor = tiles[TileId(id.z - levels, id.x shr levels, id.y shr levels)]
        if (ancestor != null) {
            val crop = ancestorCrop(id.x, id.y, levels, ancestor.width)
            drawImage(
                image = ancestor,
                srcOffset = IntOffset(crop.offsetX, crop.offsetY),
                srcSize = IntSize(crop.size, crop.size),
                dstOffset = IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
                dstSize = IntSize(TILE_PX, TILE_PX),
                filterQuality = FilterQuality.Low,
            )
            return
        }
        levels++
    }
}
