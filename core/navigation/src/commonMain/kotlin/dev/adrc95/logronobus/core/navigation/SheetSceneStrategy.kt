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

package dev.adrc95.logronobus.core.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

@OptIn(ExperimentalMaterial3Api::class)
class SheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val entry = entries.lastOrNull() ?: return null
        entry.metadata[MetadataKey] ?: return null

        return object : OverlayScene<T> {
            override val key = entry.contentKey

            override val entries: List<NavEntry<T>> = listOf(entry)

            override val previousEntries: List<NavEntry<T>> = entries.dropLast(1)

            override val overlaidEntries: List<NavEntry<T>> = entries.dropLast(1)

            lateinit var sheetState: SheetState

            override val content: @Composable () -> Unit = {
                sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
                ModalBottomSheet(
                    onDismissRequest = onBack,
                    sheetState = sheetState,
                ) {
                    entry.Content()
                }
            }

            override suspend fun onRemove() {
                sheetState.hide()
            }
        }
    }

    companion object {
        private object MetadataKey : NavMetadataKey<ModalBottomSheetProperties>

        fun sheet() = metadata { put(MetadataKey, ModalBottomSheetProperties()) }
    }
}
