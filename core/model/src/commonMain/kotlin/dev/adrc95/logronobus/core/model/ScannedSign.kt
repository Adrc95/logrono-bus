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

package dev.adrc95.logronobus.core.model

data class ScannedText(
    val text: String,
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val colour: Int = 0,
    val dot: Int = 0,
)

data class ScannedStop(
    val name: String,
    val isMarked: Boolean,
    val mark: Int = 0,
)

data class ScannedSign(
    val line: String? = null,
    val lines: List<String> = emptyList(),
    val directions: List<String> = emptyList(),
    val stops: List<ScannedStop> = emptyList(),
)
