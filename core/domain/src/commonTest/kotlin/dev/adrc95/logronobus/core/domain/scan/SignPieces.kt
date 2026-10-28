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

internal fun signPieces(
    line: String,
    directions: List<String>,
    stops: List<String>,
    marked: String? = null,
    letterBeside: String? = null,
    nightName: String? = null,
): List<ScannedText> {
    val pieces = mutableListOf<ScannedText>()
    pieces +=
        ScannedText(
            text = line,
            left = 100f,
            top = 40f,
            width = 180f,
            height = 160f,
        )
    letterBeside?.let { letter ->
        pieces +=
            ScannedText(
                text = letter,
                left = 290f,
                top = 60f,
                width = 150f,
                height = 130f,
            )
    }
    nightName?.let { nickname ->
        pieces +=
            ScannedText(
                text = nickname,
                left = 110f,
                top = 150f,
                width = 260f,
                height = 50f,
            )
    }
    directions.forEachIndexed { index, name ->
        pieces +=
            ScannedText(
                text = name,
                left = 100f,
                top = 220f + index * 130f,
                width = 300f,
                height = 55f,
            )
    }
    stops.forEachIndexed { index, name ->
        pieces +=
            ScannedText(
                text = name,
                left = 520f,
                top = 40f + index * 60f,
                width = 380f,
                height = 40f,
                colour = if (name == marked) 80 else 5,
            )
    }
    return pieces
}
