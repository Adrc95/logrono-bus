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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal fun busGlyph(): ImageVector =
    ImageVector
        .Builder(
            name = "Bus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.White)) {
            moveTo(4f, 16f)
            curveTo(4f, 16.88f, 4.39f, 17.67f, 5f, 18.22f)
            verticalLineTo(20f)
            curveTo(5f, 20.55f, 5.45f, 21f, 6f, 21f)
            horizontalLineTo(7f)
            curveTo(7.55f, 21f, 8f, 20.55f, 8f, 20f)
            verticalLineTo(19f)
            horizontalLineTo(16f)
            verticalLineTo(20f)
            curveTo(16f, 20.55f, 16.45f, 21f, 17f, 21f)
            horizontalLineTo(18f)
            curveTo(18.55f, 21f, 19f, 20.55f, 19f, 20f)
            verticalLineTo(18.22f)
            curveTo(19.61f, 17.67f, 20f, 16.88f, 20f, 16f)
            verticalLineTo(6f)
            curveTo(20f, 2.5f, 16.42f, 2f, 12f, 2f)
            reflectiveCurveTo(4f, 2.5f, 4f, 6f)
            verticalLineTo(16f)
            close()
            moveTo(7.5f, 17f)
            curveTo(6.67f, 17f, 6f, 16.33f, 6f, 15.5f)
            reflectiveCurveTo(6.67f, 14f, 7.5f, 14f)
            reflectiveCurveTo(9f, 14.67f, 9f, 15.5f)
            reflectiveCurveTo(8.33f, 17f, 7.5f, 17f)
            close()
            moveTo(16.5f, 17f)
            curveTo(15.67f, 17f, 15f, 16.33f, 15f, 15.5f)
            reflectiveCurveTo(15.67f, 14f, 16.5f, 14f)
            reflectiveCurveTo(18f, 14.67f, 18f, 15.5f)
            reflectiveCurveTo(17.33f, 17f, 16.5f, 17f)
            close()
            moveTo(18f, 11f)
            horizontalLineTo(6f)
            verticalLineTo(6f)
            horizontalLineTo(18f)
            verticalLineTo(11f)
            close()
        }.build()
