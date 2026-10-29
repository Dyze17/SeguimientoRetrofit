package co.edu.uniquindio.pokeapi.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.pokeapi.domain.model.AppError
import co.edu.uniquindio.pokeapi.domain.model.AppException
import co.edu.uniquindio.pokeapi.domain.model.PokemonDetail
import co.edu.uniquindio.pokeapi.domain.repository.NetworkMonitor
import co.edu.uniquindio.pokeapi.domain.repository.PokemonRepository
import co.edu.uniquindio.pokeapi.ui.navigation.PokemonDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetail) : PokemonDetailUiState
    data class Error(val error: AppError) : PokemonDetailUiState
}

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    /** El número que llega en la ruta de navegación. */
    val pokemonId: Int = savedStateHandle.toRoute<PokemonDetailRoute>().id

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = true)

    private var loadJob: Job? = null

    init {
        load()
        // Si el detalle falló por falta de internet, se carga solo al volver la conexión.
        viewModelScope.launch {
            isOnline.filter { it }.collect {
                if ((_uiState.value as? PokemonDetailUiState.Error)?.error == AppError.Network) load()
            }
        }
    }

    fun retry() = load()

    private fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            _uiState.value = try {
                PokemonDetailUiState.Success(repository.getPokemonDetail(pokemonId))
            } catch (e: AppException) {
                PokemonDetailUiState.Error(e.error)
            }
        }
    }
}
