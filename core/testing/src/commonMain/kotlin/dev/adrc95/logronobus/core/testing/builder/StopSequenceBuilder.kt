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

package dev.adrc95.logronobus.core.testing.builder

import dev.adrc95.logronobus.core.model.StopSequence

class StopSequenceBuilder {
    var ids: List<Int> = listOf(152)
    var terminal: String? = null

    fun withIds(ids: List<Int>) = apply { this.ids = ids }

    fun withTerminal(terminal: String?) = apply { this.terminal = terminal }

    fun build() = StopSequence(ids = ids, terminal = terminal)
}

fun stopSequence(block: StopSequenceBuilder.() -> Unit = {}) = StopSequenceBuilder().apply(block).build()
