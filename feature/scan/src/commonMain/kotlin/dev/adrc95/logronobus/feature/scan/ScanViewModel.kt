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

package dev.adrc95.logronobus.feature.scan

import androidx.lifecycle.ViewModel
import dev.adrc95.logronobus.core.domain.usecase.AskForStopUseCase
import dev.adrc95.logronobus.core.domain.usecase.ReadStopSignUseCase
import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.core.model.StopSignReading
import dev.adrc95.logronobus.core.ui.mapper.toDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.launchSafely
import dev.adrc95.logronobus.core.ui.util.routeName
import dev.adrc95.logronobus.core.ui.util.safeCall
import dev.adrc95.logronobus.core.ui.util.stateInViewModel
import dev.adrc95.logronobus.core.ui.util.titleCased
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScanViewModel(
    private val readStopSign: ReadStopSignUseCase,
    private val askForStop: AskForStopUseCase,
) : ViewModel() {
    private val reading = MutableStateFlow<StopSignReading?>(null)
    private val withoutMarkedStop = MutableStateFlow(false)
    private val chosenStop = MutableStateFlow<StopDisplayModel?>(null)
    private val hidden = MutableStateFlow(false)
    private val framesWithoutStop = MutableStateFlow(0)

    val uiState: StateFlow<UiState> =
        combine(reading, withoutMarkedStop, chosenStop, hidden) { reading, withoutMarkedStop, chosenStop, hidden ->
            UiState(
                line = reading?.line,
                direction = reading?.direction,
                preview = reading?.stop?.toDisplayModel()?.name,
                name = reading?.displayName(),
                askForStop = withoutMarkedStop && chosenStop == null && !hidden,
                stop = chosenStop ?: reading?.stop?.toDisplayModel(),
                candidates =
                    reading
                        ?.candidates
                        .orEmpty()
                        .map { it.toDisplayModel() }
                        .toImmutableList(),
            )
        }.stateInViewModel(this, UiState())

    fun onPieces(pieces: List<ScannedText>) {
        launchSafely {
            val result =
                safeCall<StopSignReading?>(null) { readStopSign(pieces, reading.value) }
                    ?: return@launchSafely
            framesWithoutStop.update { if (result.stop == null) it + 1 else 0 }
            val previous = reading.value
            if (previous?.line != result.line) {
                chosenStop.value = null
                withoutMarkedStop.value = false
                hidden.value = false
            }
            reading.value = result
            if (result.stop != null) {
                withoutMarkedStop.value = false
            } else if (askForStop(result, framesWithoutStop.value)) {
                withoutMarkedStop.value = true
            }
        }
    }

    fun chooseStop(stop: StopDisplayModel) {
        chosenStop.value = stop
    }

    fun keepScanning() {
        hidden.value = true
        withoutMarkedStop.value = false
        framesWithoutStop.value = 0
    }

    fun restart() {
        reading.value = null
        withoutMarkedStop.value = false
        chosenStop.value = null
        hidden.value = false
        framesWithoutStop.value = 0
    }

    data class UiState(
        val line: String? = null,
        val direction: Int? = null,
        val preview: String? = null,
        val name: String? = null,
        val askForStop: Boolean = false,
        val stop: StopDisplayModel? = null,
        val candidates: ImmutableList<StopDisplayModel> = persistentListOf(),
    )
}

private fun StopSignReading.displayName(): String {
    val ends = listOfNotNull(origin, destination).filter { end -> end.isNotBlank() }
    return if (ends.size == 2) {
        ends.joinToString(" - ") { end -> end.titleCased() }
    } else {
        longName.routeName()
    }
}
