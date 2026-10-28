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

package dev.adrc95.logronobus.feature.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.ui.component.BusMapView
import dev.adrc95.logronobus.core.ui.component.StopLineRow
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.cd_add_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.cd_remove_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.distance_km
import dev.adrc95.logronobus.core.ui.generated.resources.distance_m
import dev.adrc95.logronobus.core.ui.generated.resources.enable_location
import dev.adrc95.logronobus.core.ui.generated.resources.finding_stops
import dev.adrc95.logronobus.core.ui.generated.resources.location_permission_body
import dev.adrc95.logronobus.core.ui.generated.resources.location_permission_needed
import dev.adrc95.logronobus.core.ui.generated.resources.showing_city_centre
import dev.adrc95.logronobus.core.ui.generated.resources.stops_near_you
import dev.adrc95.logronobus.core.ui.generated.resources.towards
import dev.adrc95.logronobus.core.ui.model.LocationDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.feature.nearby.NearbyViewModel
import dev.adrc95.logronobus.feature.nearby.component.NearbyLoading
import dev.adrc95.logronobus.feature.nearby.component.NearbyMapAndList
import dev.adrc95.logronobus.feature.nearby.component.NearbyMessage
import dev.adrc95.logronobus.feature.nearby.model.DistanceDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyDisplayModel
import dev.adrc95.logronobus.feature.nearby.model.NearbyState
import dev.adrc95.logronobus.feature.nearby.model.NearbyStopDisplayModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun NearbyRoute(
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel = koinViewModel(),
    onStopClick: InputAction<StopDisplayModel>,
) {
    val nearbyState by viewModel.uiState.collectAsStateWithLifecycle()
    NearbyScreen(
        modifier = modifier,
        nearbyState = nearbyState,
        onReload = { viewModel.reload() },
        onStopClick = onStopClick,
        onToggleFavourite = { stop -> viewModel.toggleFavourite(stop) },
    )
}

@Composable
fun NearbyScreen(
    modifier: Modifier = Modifier,
    nearbyState: NearbyViewModel.UiState,
    onReload: NoParamAction,
    onStopClick: InputAction<StopDisplayModel>,
    onToggleFavourite: InputAction<StopDisplayModel>,
) {
    when {
        nearbyState.nearbyState is NearbyState.PermissionDenied -> {
            NearbyMessage(
                icon = LogronoIcons.LocationOff,
                title = stringResource(Res.string.location_permission_needed),
                body = stringResource(Res.string.location_permission_body),
                actionLabel = stringResource(Res.string.enable_location),
                onAction = { onReload() },
                modifier = modifier,
            )
        }

        nearbyState.nearby.stops.isEmpty() &&
            (nearbyState.nearbyState is NearbyState.Idle || nearbyState.nearbyState is NearbyState.Loading) -> {
            NearbyLoading(modifier)
        }

        else -> {
            NearbyMapAndList(
                modifier = modifier,
                nearby = nearbyState.nearby,
                userLocation = (nearbyState.nearbyState as? NearbyState.Located)?.location,
                onStopClick = { onStopClick(it) },
                onToggleFavourite = { onToggleFavourite(it) },
            )
        }
    }
}
