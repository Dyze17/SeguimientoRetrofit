package co.edu.uniquindio.pokeapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.edu.uniquindio.pokeapi.data.mapper.toPokemonSummary
import co.edu.uniquindio.pokeapi.data.remote.PokeApiService
import co.edu.uniquindio.pokeapi.data.remote.toAppException
import co.edu.uniquindio.pokeapi.domain.model.PokemonSummary
import kotlinx.coroutines.CancellationException

/**
 * La lista sin filtros: cada página es una petición GET /pokemon?offset=…&limit=… La clave de la página es el offset,
 * y la siguiente empieza donde terminó esta. La API indica en `next` si quedan más.
 */
class PokemonPagingSource(
    private val api: PokeApiService,
) : PagingSource<Int, PokemonSummary>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonSummary> {
        val offset = params.key ?: 0
        return try {
            val page = api.getPokemonList(offset = offset, limit = params.loadSize)
            LoadResult.Page(
                data = page.results.map { it.toPokemonSummary() },
                prevKey = null,
                nextKey = if (page.next == null || page.results.isEmpty()) null else offset + page.results.size,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e.toAppException())
        }
    }

    // Al recargar (deslizar hacia abajo) se vuelve a empezar desde el primero.
    override fun getRefreshKey(state: PagingState<Int, PokemonSummary>): Int? = null
}
