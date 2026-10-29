package co.edu.uniquindio.pokeapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.edu.uniquindio.pokeapi.data.remote.toAppException
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import kotlinx.coroutines.CancellationException

/**
 * La lista con filtros. /type/{name} y /generation/{id} no se paginan en la API: responden todos sus Pokémon de una
 * vez. [loadResults] los trae (el repositorio los guarda en memoria) y aquí se entregan de a una página, para que el
 * scroll infinito funcione igual que sin filtros. La clave de la página es la posición en la lista.
 */
class FilteredPokemonPagingSource(
    private val loadResults: suspend () -> List<PokemonSummary>,
) : PagingSource<Int, PokemonSummary>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonSummary> {
        return try {
            val results = loadResults()
            val start = (params.key ?: 0).coerceIn(0, results.size)
            val end = (start + params.loadSize).coerceAtMost(results.size)
            LoadResult.Page(
                data = results.subList(start, end),
                prevKey = null,
                nextKey = if (end < results.size) end else null,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e.toAppException())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PokemonSummary>): Int? = null
}
