package co.edu.uniquindio.pokeapi.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.pokeapi.R
import co.edu.uniquindio.pokeapi.domain.model.PokemonDetail
import co.edu.uniquindio.pokeapi.ui.components.ConnectionStatusChip
import co.edu.uniquindio.pokeapi.ui.components.ErrorState
import co.edu.uniquindio.pokeapi.ui.components.LoadingState
import co.edu.uniquindio.pokeapi.ui.components.OfflineBanner
import co.edu.uniquindio.pokeapi.ui.components.PokemonArtwork
import co.edu.uniquindio.pokeapi.ui.components.TypeBadge
import co.edu.uniquindio.pokeapi.ui.components.formatApiName
import co.edu.uniquindio.pokeapi.ui.components.formatPokemonNumber
import co.edu.uniquindio.pokeapi.ui.components.generationValueLabel
import co.edu.uniquindio.pokeapi.ui.components.habitatLabel
import co.edu.uniquindio.pokeapi.ui.components.statLabel
import co.edu.uniquindio.pokeapi.ui.components.typeColor

// La estadística base más alta de los juegos es 255.
private const val MAX_STAT = 255f

@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    viewModel: PokemonDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    PokemonDetailContent(
        pokemonId = viewModel.pokemonId,
        uiState = uiState,
        isOnline = isOnline,
        onBack = onBack,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonDetailContent(
    pokemonId: Int,
    uiState: PokemonDetailUiState,
    isOnline: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val title = (uiState as? PokemonDetailUiState.Success)?.pokemon?.name?.let(::formatApiName)
        ?: formatPokemonNumber(pokemonId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
                actions = { ConnectionStatusChip(isOnline = isOnline, modifier = Modifier.padding(end = 12.dp)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            OfflineBanner(visible = !isOnline)
            when (uiState) {
                PokemonDetailUiState.Loading -> LoadingState()
                is PokemonDetailUiState.Error -> ErrorState(error = uiState.error, onRetry = onRetry)
                is PokemonDetailUiState.Success -> PokemonDetailBody(pokemon = uiState.pokemon)
            }
        }
    }
}

@Composable
private fun PokemonDetailBody(pokemon: PokemonDetail) {
    val mainColor = typeColor(pokemon.types.firstOrNull().orEmpty())
    var showShiny by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        PokemonHeader(
            pokemon = pokemon,
            mainColor = mainColor,
            showShiny = showShiny,
            onShinyChange = { showShiny = it },
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DescriptionSection(pokemon)
            DataSection(pokemon)
            AbilitiesSection(pokemon)
            StatsSection(pokemon, barColor = mainColor)
        }
    }
}

@Composable
private fun PokemonHeader(
    pokemon: PokemonDetail,
    mainColor: Color,
    showShiny: Boolean,
    onShinyChange: (Boolean) -> Unit,
) {
    val name = formatApiName(pokemon.name)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(mainColor.copy(alpha = 0.35f), Color.Transparent)))
            .padding(top = 8.dp, bottom = 8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PokemonArtwork(
                imageUrl = if (showShiny && pokemon.shinyImageUrl != null) pokemon.shinyImageUrl else pokemon.imageUrl,
                name = name,
                size = 220.dp,
                backgroundColor = mainColor.copy(alpha = 0.18f),
            )
            Text(
                text = formatPokemonNumber(pokemon.id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pokemon.types.forEach { TypeBadge(type = it) }
            }
            val rarity = when {
                pokemon.species?.isMythical == true -> R.string.mythical
                pokemon.species?.isLegendary == true -> R.string.legendary
                else -> null
            }
            if (rarity != null) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        text = stringResource(rarity),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }

        if (pokemon.shinyImageUrl != null) {
            FilledTonalIconToggleButton(
                checked = showShiny,
                onCheckedChange = onShinyChange,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp),
            ) {
                Icon(painterResource(R.drawable.ic_auto_awesome), contentDescription = stringResource(R.string.shiny_toggle))
            }
        }
    }
}

@Composable
private fun DescriptionSection(pokemon: PokemonDetail) {
    DetailSection(title = stringResource(R.string.section_description)) {
        pokemon.species?.genus?.let {
            Text(text = it, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = pokemon.species?.description ?: stringResource(R.string.no_description),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DataSection(pokemon: PokemonDetail) {
    val species = pokemon.species
    val items = buildList {
        add(stringResource(R.string.height) to stringResource(R.string.height_value, pokemon.heightMeters))
        add(stringResource(R.string.weight) to stringResource(R.string.weight_value, pokemon.weightKg))
        pokemon.baseExperience?.let { add(stringResource(R.string.base_experience) to it.toString()) }
        species?.captureRate?.let { add(stringResource(R.string.capture_rate) to stringResource(R.string.capture_rate_value, it)) }
        species?.habitat?.let { add(stringResource(R.string.habitat) to habitatLabel(it)) }
        species?.generation?.let { add(stringResource(R.string.generation) to generationValueLabel(it)) }
    }

    DetailSection(title = stringResource(R.string.section_data)) {
        items.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AbilitiesSection(pokemon: PokemonDetail) {
    DetailSection(title = stringResource(R.string.section_abilities)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pokemon.abilities.forEach { ability ->
                val name = formatApiName(ability.name)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (ability.isHidden) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                ) {
                    Text(
                        text = if (ability.isHidden) stringResource(R.string.ability_hidden, name) else name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsSection(pokemon: PokemonDetail, barColor: Color) {
    DetailSection(title = stringResource(R.string.section_stats)) {
        pokemon.stats.forEach { stat ->
            StatRow(label = statLabel(stat.name), value = stat.value, barColor = barColor)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.stat_total),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(88.dp),
            )
            Text(
                text = pokemon.stats.sumOf { it.value }.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp),
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int, barColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(88.dp),
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { (value / MAX_STAT).coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
