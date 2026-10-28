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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.theme.logronoCardColors
import dev.adrc95.logronobus.core.model.ScannedText
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.scan_choose_stop
import dev.adrc95.logronobus.core.ui.generated.resources.scan_finding_stop
import dev.adrc95.logronobus.core.ui.generated.resources.scan_keep_scanning
import dev.adrc95.logronobus.core.ui.generated.resources.scan_point_camera
import dev.adrc95.logronobus.core.ui.generated.resources.scan_reading
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.scan.CameraTextScanner
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanRoute(
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = koinViewModel(),
    sheetOpen: Boolean,
    onStopClick: (StopDisplayModel, String?, Int?) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(sheetOpen) {
        if (!sheetOpen) viewModel.restart()
    }
    ScanScreen(
        sheetOpen = sheetOpen,
        state = state,
        onPieces = { pieces -> if (!sheetOpen) viewModel.onPieces(pieces) },
        onStopSelected = { stop -> onStopClick(stop, state.line, state.direction) },
        onKeepScanning = { viewModel.keepScanning() },
        modifier = modifier,
    )
}

@Composable
fun ScanScreen(
    sheetOpen: Boolean,
    state: ScanViewModel.UiState,
    onPieces: InputAction<List<ScannedText>>,
    onStopSelected: InputAction<StopDisplayModel>,
    onKeepScanning: InputAction<Unit>,
    modifier: Modifier = Modifier,
) {
    var handledStopRef by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(sheetOpen) {
        if (!sheetOpen) handledStopRef = null
    }
    LaunchedEffect(state.stop) {
        val stop = state.stop ?: return@LaunchedEffect
        if (stop.stopRef != handledStopRef) {
            handledStopRef = stop.stopRef
            onStopSelected(stop)
        }
    }

    val hint = stringResource(Res.string.scan_point_camera)
    val reading = stringResource(Res.string.scan_reading)
    val finding = stringResource(Res.string.scan_finding_stop)
    val status =
        when {
            state.line == null -> reading
            state.stop != null -> state.stop.name
            state.preview != null -> state.preview
            else -> finding
        }
    val title = listOfNotNull(state.line, state.name).joinToString(" · ").ifEmpty { hint }

    Box(modifier) {
        CameraTextScanner(
            onPieces = onPieces,
            modifier = Modifier.fillMaxSize(),
        )
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
            colors = logronoCardColors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    LogronoIcons.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        status,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        if (state.stop == null && state.askForStop && state.candidates.isNotEmpty()) {
            Card(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .padding(16.dp),
                colors = logronoCardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = listOfNotNull(state.line, state.name).joinToString(" · "),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        )
                        TextButton(onClick = { onKeepScanning(Unit) }) {
                            Text(stringResource(Res.string.scan_keep_scanning))
                        }
                    }
                    HorizontalDivider()
                    Text(
                        stringResource(Res.string.scan_choose_stop),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(state.candidates, key = { it.stopRef }) { stop ->
                            Text(
                                text = stop.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onStopSelected(stop) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
