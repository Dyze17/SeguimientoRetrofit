package co.edu.uniquindio.pokeapi.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import co.edu.uniquindio.pokeapi.R
import co.edu.uniquindio.pokeapi.domain.model.PokemonFilter
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import co.edu.uniquindio.pokeapi.ui.components.ConnectionStatusChip
import co.edu.uniquindio.pokeapi.ui.components.ErrorState
import co.edu.uniquindio.pokeapi.ui.components.LoadingState
import co.edu.uniquindio.pokeapi.ui.components.MessageState
import co.edu.uniquindio.pokeapi.ui.components.OfflineBanner
import co.edu.uniquindio.pokeapi.ui.components.PokemonArtwork
import co.edu.uniquindio.pokeapi.ui.components.formatApiName
import co.edu.uniquindio.pokeapi.ui.components.formatPokemonNumber
import co.edu.uniquindio.pokeapi.ui.components.messageRes
import co.edu.uniquindio.pokeapi.ui.components.toAppError
import kotlinx.coroutines.flow.drop

@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val appliedFilter by viewModel.appliedFilter.collectAsStateWithLifecycle()
    val pokemon = viewModel.pokemon.collectAsLazyPagingItems()

    PokemonListContent(
        uiState = uiState,
        isOnline = isOnline,
        appliedFilter = appliedFilter,
        pokemon = pokemon,
        onQueryChange = viewModel::onQueryChange,
        onTypeSelected = viewModel::onTypeSelected,
        onGenerationSelected = viewModel::onGenerationSelected,
        onClearFilters = viewModel::clearFilters,
        onRetryFilterOptions = viewModel::loadFilterOptions,
        onPokemonClick = onPokemonClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonListContent(
    uiState: PokemonListUiState,
    isOnline: Boolean,
    appliedFilter: PokemonFilter,
    pokemon: LazyPagingItems<PokemonSummary>,
    onQueryChange: (String) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onGenerationSelected: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    onRetryFilterOptions: () -> Unit,
    onPokemonClick: (Int) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    // Al volver la conexión se reintenta lo que falló, sin que la persona tenga que tocar «Reintentar».
    LaunchedEffect(isOnline) {
        val loadState = pokemon.loadState
        if (isOnline && (loadState.refresh is LoadState.Error || loadState.append is LoadState.Error)) pokemon.retry()
    }

    // Con un filtro nuevo la lista vuelve arriba. drop(1): al volver del detalle se conserva la posición.
    val currentFilter by rememberUpdatedState(appliedFilter)
    LaunchedEffect(gridState) {
        snapshotFlow { currentFilter }.drop(1).collect { gridState.scrollToItem(0) }
    }

    // Si falla una recarga con la lista ya visible, se avisa con un snackbar en lugar de taparla.
    val refreshError = (pokemon.loadState.refresh as? LoadState.Error)?.error?.toAppError()
    val refreshErrorMessage = refreshError?.let { stringResource(it.messageRes()) }
    LaunchedEffect(refreshErrorMessage) {
        if (refreshErrorMessage != null && pokemon.itemCount > 0) snackbarHostState.showSnackbar(refreshErrorMessage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = { ConnectionStatusChip(isOnline = isOnline, modifier = Modifier.padding(end = 12.dp)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            OfflineBanner(visible = !isOnline)
            PokemonFilters(
                uiState = uiState,
                onQueryChange = onQueryChange,
                onTypeSelected = onTypeSelected,
                onGenerationSelected = onGenerationSelected,
                onClearFilters = onClearFilters,
                onRetryFilterOptions = onRetryFilterOptions,
                modifier = Modifier.padding(top = 8.dp),
            )
            PullToRefreshBox(
                isRefreshing = pokemon.loadState.refresh is LoadState.Loading && pokemon.itemCount > 0,
                onRefresh = pokemon::refresh,
                modifier = Modifier.weight(1f),
            ) {
                PokemonGrid(
                    pokemon = pokemon,
                    gridState = gridState,
                    onPokemonClick = onPokemonClick,
                    onClearFilters = onClearFilters,
                )
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemon: LazyPagingItems<PokemonSummary>,
    gridState: LazyGridState,
    onPokemonClick: (Int) -> Unit,
    onClearFilters: () -> Unit,
) {
    val refresh = pokemon.loadState.refresh

    // Sin elementos todavía: la primera página está cargando, falló o no hay resultados.
    if (pokemon.itemCount == 0) {
        when {
            refresh is LoadState.Error -> ErrorState(error = refresh.error.toAppError(), onRetry = pokemon::retry)
            refresh is LoadState.NotLoading && pokemon.loadState.append.endOfPaginationReached -> MessageState(
                icon = R.drawable.ic_search_off,
                title = stringResource(R.string.empty_results),
                message = stringResource(R.string.empty_results_hint),
                actionLabel = stringResource(R.string.clear_filters),
                onAction = onClearFilters,
            )
            else -> LoadingState()
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            count = pokemon.itemCount,
            key = pokemon.itemKey { it.id },
            contentType = pokemon.itemContentType { "pokemon" },
        ) { index ->
            pokemon[index]?.let { item ->
                PokemonCard(pokemon = item, onClick = { onPokemonClick(item.id) })
            }
        }

        // El pie de la lista: cargando la página siguiente, error al cargarla, o fin de la lista.
        when (val append = pokemon.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                AppendErrorFooter(message = stringResource(append.error.toAppError().messageRes()), onRetry = pokemon::retry)
            }
            is LoadState.NotLoading -> if (append.endOfPaginationReached) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.end_of_list),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(pokemon: PokemonSummary, onClick: () -> Unit) {
    val name = formatApiName(pokemon.name)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatPokemonNumber(pokemon.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )
            PokemonArtwork(imageUrl = pokemon.imageUrl, name = name, size = 112.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppendErrorFooter(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.load_more_error), style = MaterialTheme.typography.titleSmall)
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}
