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

import androidx.compose.ui.graphics.Color

internal const val TILE_URL_BASE = "https://tile.openstreetmap.org"

internal val ROUTE_LINE_COLOR = Color(0xFF1565C0)
internal val ETA_TEXT_COLOR = Color(0xFF7B1FA2)
internal val ETA_TEXT_PAST_COLOR = Color(0xFF757575)
internal val TRACKED_HALO_COLOR = Color(0xFF4F0000)
internal val MARKER_SHADOW_COLOR = Color(0x99000000)
internal val STOP_DEFAULT_COLOR = Color(0xFF37474F)
internal val STOP_PAST_COLOR = Color(0xFF9E9E9E)
internal val USER_LOCATION_ACCENT = Color(0xFF1E88E5)
internal val GLYPH_TINT_ON_LIGHT = Color(0xFF1A1A1A)

internal val BUS_PALETTE =
    listOf(
        Color(0xFFE53935),
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFF8E24AA),
        Color(0xFF00ACC1),
        Color(0xFFC0CA33),
        Color(0xFFF4511E),
        Color(0xFFD81B60),
        Color(0xFF3949AB),
        Color(0xFF00897B),
        Color(0xFFFDD835),
        Color(0xFF5E35B1),
        Color(0xFF039BE5),
        Color(0xFF7CB342),
        Color(0xFFFFB300),
        Color(0xFF6D4C41),
        Color(0xFF546E7A),
    )

internal const val BUS_MARKER_RADIUS = 20f
internal const val BUS_MARKER_SHADOW_OFFSET = 2f
internal const val BUS_NOSE_LENGTH = 12f
internal const val BUS_NOSE_BASE = 4f
internal const val BUS_NOSE_HALF_WIDTH = 9f
internal const val BUS_GLYPH_SIZE = 24f
internal const val BUS_SQUARE_CORNER = 6f
internal const val BUS_LUMINANCE_THRESHOLD = 0.5f

internal const val STOP_MARKER_RADIUS = 11f
internal const val TRACKED_STOP_RADIUS = 16f
internal const val STOP_MARKER_BORDER = 2.5f
internal const val STOP_MARKER_MARGIN = 16f

internal const val ETA_LABEL_GAP = 5f
internal const val ETA_BOX_PADDING_X = 3f
internal const val ETA_BOX_PADDING_Y = 2f
internal const val ETA_BOX_CORNER = 4f
internal const val ETA_BOX_ALPHA = 0.85f

internal const val ROUTE_LINE_WIDTH = 5f
internal const val ROUTE_CASING_WIDTH = 9f
internal const val ROUTE_CASING_ALPHA = 0.7f

internal const val ARROW_OUTER_TIP = 7f
internal const val ARROW_OUTER_BASE = -5f
internal const val ARROW_OUTER_HALF_WIDTH = 6f
internal const val ARROW_INNER_TIP = 5f
internal const val ARROW_INNER_BASE = -3f
internal const val ARROW_INNER_HALF_WIDTH = 4f

internal const val USER_LOCATION_HALO_RADIUS = 26f
internal const val USER_LOCATION_HALO_ALPHA = 0.18f
internal const val USER_LOCATION_RING_RADIUS = 10f
internal const val USER_LOCATION_DOT_RADIUS = 7f
internal const val USER_LOCATION_MARGIN = 20f

internal const val TAP_BUS_RADIUS = 30f
internal const val TAP_STOP_RADIUS = 28f
internal const val HOVER_STOP_SLACK = 8f
