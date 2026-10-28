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

package dev.adrc95.logronobus.feature.settings.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.ok
import dev.adrc95.logronobus.core.ui.generated.resources.restart_to_apply_body
import dev.adrc95.logronobus.core.ui.generated.resources.restart_to_apply_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RestartForLanguageDialog(onDismiss: NoParamAction) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.restart_to_apply_title),
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.restart_to_apply_body),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.ok),
                )
            }
        },
    )
}
