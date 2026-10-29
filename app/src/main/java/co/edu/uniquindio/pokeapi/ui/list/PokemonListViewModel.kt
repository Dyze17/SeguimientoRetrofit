package co.edu.uniquindio.pokeapi.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException
import co.edu.uniquindio.pokeapi.domain.model.Generation
import co.edu.uniquindio.pokeapi.domain.model.PokemonFilter
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import co.edu.uniquindio.pokeapi.domain.repository.NetworkMonitor
import co.edu.uniquindio.pokeapi.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonListUiState(
    val query: String = "",
    val selectedType: String? = null,
    val selectedGenerationId: Int? = null,
    val types: List<String> = emptyList(),
    val generations: List<Generation> = emptyList(),
    val filterOptionsError: AppError? = null,
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || selectedType != null || selectedGenerationId != null
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = true)

    /** El filtro que se está aplicando: el texto espera a que se deje de escribir; tipo y generación, no. */
    val appliedFilter: StateFlow<PokemonFilter> = combine(
        _uiState.map { it.query.trim() }
            .distinctUntilChanged()
            .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
        _uiState.map { it.selectedType }.distinctUntilChanged(),
        _uiState.map { it.selectedGenerationId }.distinctUntilChanged(),
    ) { query, type, generationId -> PokemonFilter(query, type, generationId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PokemonFilter())

    /** Cada filtro nuevo arranca una paginación nueva; cachedIn conserva las páginas al volver del detalle. */
    val pokemon: Flow<PagingData<PokemonSummary>> = appliedFilter
        .flatMapLatest { repository.getPokemonPages(it) }
        .cachedIn(viewModelScope)

    private var filterOptionsJob: Job? = null

    init {
        loadFilterOptions()
        // Si los filtros no cargaron por falta de internet, se piden de nuevo en cuanto vuelve la conexión.
        viewModelScope.launch {
            isOnline.filter { it }.collect {
                if (_uiState.value.filterOptionsError != null) loadFilterOptions()
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onGenerationSelected(generationId: Int?) {
        _uiState.update { it.copy(selectedGenerationId = generationId) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(query = "", selectedType = null, selectedGenerationId = null) }
    }

    fun loadFilterOptions() {
        if (filterOptionsJob?.isActive == true) return
        filterOptionsJob = viewModelScope.launch {
            _uiState.update { it.copy(filterOptionsError = null) }
            try {
                val (types, generations) = coroutineScope {
                    val types = async { repository.getTypes() }
                    val generations = async { repository.getGenerations() }
                    types.await() to generations.await()
                }
                _uiState.update { it.copy(types = types, generations = generations) }
            } catch (e: AppException) {
                _uiState.update { it.copy(filterOptionsError = e.error) }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
