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

import dev.adrc95.logronobus.core.model.StopEta

class StopEtaBuilder {
    var label: String = "12:00"
    var upcoming: Boolean = true

    fun withLabel(label: String) = apply { this.label = label }

    fun withUpcoming(upcoming: Boolean) = apply { this.upcoming = upcoming }

    fun build() = StopEta(label = label, upcoming = upcoming)
}

fun stopEta(block: StopEtaBuilder.() -> Unit = {}) = StopEtaBuilder().apply(block).build()
