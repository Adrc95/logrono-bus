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

package dev.adrc95.logronobus.feature.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.adrc95.logronobus.core.common.DoubleInputAction
import dev.adrc95.logronobus.core.common.InputAction
import dev.adrc95.logronobus.core.common.NoParamAction
import dev.adrc95.logronobus.core.common.platform.nowEpochMilliseconds
import dev.adrc95.logronobus.core.designsystem.component.LogronoIcons
import dev.adrc95.logronobus.core.designsystem.component.RouteBadge
import dev.adrc95.logronobus.core.designsystem.theme.DarkColors
import dev.adrc95.logronobus.core.designsystem.theme.LightColors
import dev.adrc95.logronobus.core.model.DepartureTime
import dev.adrc95.logronobus.core.ui.component.DepartureTimeText
import dev.adrc95.logronobus.core.ui.component.TrackChevron
import dev.adrc95.logronobus.core.ui.generated.resources.Res
import dev.adrc95.logronobus.core.ui.generated.resources.browse_routes
import dev.adrc95.logronobus.core.ui.generated.resources.cd_add_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.cd_clear_search
import dev.adrc95.logronobus.core.ui.generated.resources.cd_exit_search
import dev.adrc95.logronobus.core.ui.generated.resources.cd_remove_favourite
import dev.adrc95.logronobus.core.ui.generated.resources.last_updated_retrying
import dev.adrc95.logronobus.core.ui.generated.resources.loading_departures
import dev.adrc95.logronobus.core.ui.generated.resources.no_more_departures
import dev.adrc95.logronobus.core.ui.generated.resources.no_saved_stops
import dev.adrc95.logronobus.core.ui.generated.resources.no_saved_stops_body
import dev.adrc95.logronobus.core.ui.generated.resources.no_stops_match
import dev.adrc95.logronobus.core.ui.generated.resources.removed_from_my_stops
import dev.adrc95.logronobus.core.ui.generated.resources.saved_stops
import dev.adrc95.logronobus.core.ui.generated.resources.saved_to_my_stops
import dev.adrc95.logronobus.core.ui.generated.resources.search_all_stops
import dev.adrc95.logronobus.core.ui.generated.resources.stop_label
import dev.adrc95.logronobus.core.ui.generated.resources.towards
import dev.adrc95.logronobus.core.ui.generated.resources.updated
import dev.adrc95.logronobus.core.ui.generated.resources.updating
import dev.adrc95.logronobus.core.ui.model.DepartureDestination
import dev.adrc95.logronobus.core.ui.model.DepartureDisplayModel
import dev.adrc95.logronobus.core.ui.model.StopDisplayModel
import dev.adrc95.logronobus.core.ui.util.REFRESH_INTERVAL_MILLIS
import dev.adrc95.logronobus.core.ui.util.rememberNowMs
import dev.adrc95.logronobus.core.ui.util.timeAgoLabel
import dev.adrc95.logronobus.feature.favourites.FavouritesViewModel
import dev.adrc95.logronobus.feature.favourites.component.FavouriteStopCard
import dev.adrc95.logronobus.feature.favourites.component.FavouritesList
import dev.adrc95.logronobus.feature.favourites.component.NoSavedStops
import dev.adrc95.logronobus.feature.favourites.component.StopSearchField
import dev.adrc95.logronobus.feature.favourites.component.StopSearchResults
import dev.adrc95.logronobus.feature.favourites.model.FavouriteStopDisplayModel
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesRoute(
    modifier: Modifier = Modifier,
    viewModel: FavouritesViewModel = koinViewModel(),
    onBrowseRoutes: NoParamAction,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    onShowMessage: InputAction<String>,
    onStopClick: InputAction<StopDisplayModel>,
) {
    val favouritesState by viewModel.uiState.collectAsStateWithLifecycle()
    FavouritesScreen(
        modifier = modifier,
        favouritesState = favouritesState,
        nowMs = rememberNowMs(),
        onBrowseRoutes = onBrowseRoutes,
        onDepartureClick = onDepartureClick,
        onShowMessage = onShowMessage,
        onStopClick = onStopClick,
        onToggleFavourite = { stop -> viewModel.toggleFavourite(stop) },
        onRefresh = { viewModel.refreshFavouriteDepartures() },
        onRemove = { ref -> viewModel.removeFavourite(ref) },
        onSearch = { viewModel.search(it) },
    )
}

@Composable
fun FavouritesScreen(
    modifier: Modifier = Modifier,
    favouritesState: FavouritesViewModel.UiState,
    nowMs: Long,
    onBrowseRoutes: NoParamAction,
    onDepartureClick: DoubleInputAction<DepartureDisplayModel, String>,
    onShowMessage: InputAction<String>,
    onStopClick: InputAction<StopDisplayModel>,
    onToggleFavourite: InputAction<StopDisplayModel>,
    onRefresh: NoParamAction,
    onRemove: InputAction<String>,
    onSearch: InputAction<String>,
) {
    val focusManager = LocalFocusManager.current
    val savedMessage = stringResource(Res.string.saved_to_my_stops)
    val removedMessage = stringResource(Res.string.removed_from_my_stops)
    Column(modifier) {
        StopSearchField(
            modifier = Modifier.fillMaxWidth(),
            query = favouritesState.query,
            onQueryChange = { onSearch(it) },
            onExit = { onSearch("") },
            onClear = { onSearch("") },
        )
        when {
            favouritesState.isSearching -> {
                val favouriteRefs = favouritesState.cards.map { it.stopRef }.toSet()
                StopSearchResults(
                    results = favouritesState.searchResults,
                    favouriteRefs = favouriteRefs,
                    lineColors = favouritesState.lineColors,
                    onToggleFavourite = { stop ->
                        val wasFavourite = stop.stopRef in favouriteRefs
                        onToggleFavourite(stop)
                        onShowMessage(if (wasFavourite) removedMessage else savedMessage)
                    },
                    onStopClick = { onStopClick(it) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }

            favouritesState.cards.isEmpty() -> {
                NoSavedStops(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    onBrowseRoutes = onBrowseRoutes,
                )
            }

            else -> {
                FavouritesList(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    cards = favouritesState.cards,
                    savedCount = favouritesState.cards.size,
                    nowMs = nowMs,
                    updatedMs = favouritesState.updatedMs,
                    isRefreshing = favouritesState.isLoading,
                    onRefresh = { onRefresh() },
                    onRemove = { ref -> onRemove(ref) },
                    onDepartureClick = { row, ref -> onDepartureClick(row, ref) },
                )
            }
        }
    }
}
