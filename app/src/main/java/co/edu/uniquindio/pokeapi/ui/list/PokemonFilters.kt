package co.edu.uniquindio.pokeapi.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.pokeapi.R
import co.edu.uniquindio.pokeapi.domain.model.Generation
import co.edu.uniquindio.pokeapi.ui.components.generationLongLabel
import co.edu.uniquindio.pokeapi.ui.components.generationShortLabel
import co.edu.uniquindio.pokeapi.ui.components.typeColor
import co.edu.uniquindio.pokeapi.ui.components.typeLabel

/** La búsqueda por nombre o número y los filtros por tipo y por generación. */
@Composable
fun PokemonFilters(
    uiState: PokemonListUiState,
    onQueryChange: (String) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onGenerationSelected: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    onRetryFilterOptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DropdownFilterChip(
                label = uiState.selectedType?.let { typeLabel(it) } ?: stringResource(R.string.filter_type),
                allOptionsLabel = stringResource(R.string.filter_type_all),
                options = uiState.types,
                selectedOption = uiState.selectedType,
                optionLabel = { typeLabel(it) },
                optionLeading = { type ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(typeColor(type), CircleShape),
                    )
                },
                onOptionSelected = onTypeSelected,
            )

            val selectedGeneration = uiState.generations.firstOrNull { it.id == uiState.selectedGenerationId }
            DropdownFilterChip(
                label = selectedGeneration?.let { generationShortLabel(it.name) }
                    ?: stringResource(R.string.filter_generation),
                allOptionsLabel = stringResource(R.string.filter_generation_all),
                options = uiState.generations,
                selectedOption = selectedGeneration,
                optionLabel = { generationLongLabel(it.name) },
                onOptionSelected = { generation: Generation? -> onGenerationSelected(generation?.id) },
            )

            if (uiState.hasActiveFilters) {
                TextButton(onClick = onClearFilters) { Text(stringResource(R.string.clear_filters)) }
            }
        }

        if (uiState.filterOptionsError != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.filter_options_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onRetryFilterOptions) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

/** Un chip que abre la lista de opciones; la primera opción quita el filtro. */
@Composable
private fun <T : Any> DropdownFilterChip(
    label: String,
    allOptionsLabel: String,
    options: List<T>,
    selectedOption: T?,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T?) -> Unit,
    optionLeading: (@Composable (T) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selectedOption != null,
            onClick = { expanded = true },
            enabled = options.isNotEmpty(),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 400.dp),
        ) {
            DropdownMenuItem(
                text = { Text(allOptionsLabel) },
                onClick = {
                    onOptionSelected(null)
                    expanded = false
                },
                trailingIcon = { if (selectedOption == null) CheckIcon() },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = optionLeading?.let { leading -> { leading(option) } },
                    trailingIcon = { if (option == selectedOption) CheckIcon() },
                )
            }
        }
    }
}

@Composable
private fun CheckIcon() {
    Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
}
